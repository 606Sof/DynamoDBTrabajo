import fastapi
import uvicorn
import requests
import os
import json
import pandas as pd

URLBase = 'https://opendata.aemet.es/opendata'
ENDPoint = '/api/prediccion/especifica/municipio/horaria'
key = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb3JnZS5jYXN0ZWxsYW5vczNAZWR1Y2EubWFkcmlkLm9yZyIsImp0aSI6IjQ4NTFhYzM2LTM3YjUtNDA5Mi05ZmI4LTYzODYyNmQ1YzY0NiIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNzM3Mzc3NzQ0LCJ1c2VySWQiOiI0ODUxYWMzNi0zN2I1LTQwOTItOWZiOC02Mzg2MjZkNWM2NDYiLCJyb2xlIjoiIn0.1fSjXOL7Vx83LvIEZWn8Y66r1XoJKUcb_Y8qRGycqBg'
auth = {"api_key":key}

excelPath = os.path.dirname(__file__) + '\\20codmun.xlsx'

app = fastapi.FastAPI()

@app.get("/prediccion/{localidad}")
def localidad(localidad: str):
    codigo = recuperarCodigo(localidad)
    url = URLBase + ENDPoint +'/'+ str (codigo)
    print(url)
    respuesta = requests.get(url, params=auth)
    if(respuesta.status_code == 200):
            respuestaJson = respuesta.json()
            datos = respuestaJson['datos']

            respuesta2Json = requests.get(datos)
            prediccionArray = respuesta2Json.json()
            prediccionDict = prediccionArray[0]

            municipio = prediccionDict['nombre']
            provincia = prediccionDict['provincia']

            prediccionDia = prediccionDict['prediccion']['dia'][0]

            estadoCielo = prediccionDia['estadoCielo'][0:6]

            temperatura = prediccionDia['temperatura'][0:6]

            precipitacion = prediccionDia['precipitacion'][0:6]

            viento = prediccionDia['vientoAndRachaMax'][0:6]
           
            jsonDevolver = {'municipio':municipio, 'provincia':provincia, 'estado del cielo':estadoCielo, 'temperatura':temperatura, 'precipitacion':precipitacion, 'viento':viento}


        
    return (jsonDevolver)

def recuperarCodigo(localidad):
    codigo = ''
    
    excel = pd.read_excel(excelPath)
    registro = excel[excel['NOMBRE'] == localidad]
   
    cpro = str (registro.iloc[0,1])
    if len(cpro) < 2:
         cpro = '0'+cpro

    cmun = str (registro.iloc[0,2])
    if len(cmun) < 2:
        cmun = '00'+cmun
    elif len(cmun) < 3:
        cmun = '0'+cmun

    return cpro+cmun

if __name__ == "__main__":
    uvicorn.run(app, host="localhost", port=1224)