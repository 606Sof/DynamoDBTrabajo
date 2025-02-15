package es.aad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.restaurantes.Cliente;
import es.restaurantes.Comida;
import es.restaurantes.Reserva;
import es.restaurantes.Restaurante;
import es.restaurantes.TipoComida;
import es.restaurantes.Trabajador;

public class JsonManager {
	private static JSONArray restaurantesArray;
	private static final Logger LOG = LoggerFactory.getLogger(JsonManager.class);
	private static final String ficheroJson = "restaurantes.json";	
	
	private static Boolean parsearJSON(File fichero)
	{	
		FileReader fr = null;
		BufferedReader br = null;
		String linea = null;
		StringBuilder sb = null;

		if(!fichero.exists()) {
		LOG.error("El fichero [" + fichero.getAbsolutePath() + "] no existe o no se ha localizado.");
		return false;
		} 
		if (!fichero.canRead()) {
		LOG.error("No se disponen permisos de lectura sobre el fichero: [" + fichero.getAbsolutePath() + "]");
		return false;
		}	
		
		try 
		{
			fr = new FileReader(fichero);
			br = new BufferedReader(fr);
			sb = new StringBuilder();
			
			while((linea = br.readLine()) != null) 
			{
				sb.append(linea);
			}
		} 
		catch (FileNotFoundException e) 
		{
			LOG.error("Error, no se ha podido encontrar el fichero. "+e.getMessage());
		} 
		catch (IOException e) {
			LOG.error("Error al acceder al fichero. "+e.getMessage());
		}
		finally 
		{
			if(br != null) 
			{
				try {
					br.close();
				} catch (IOException e) {
					LOG.error("Error al cerrar el lector del fichero. " + e.getMessage());
				}
			}			
		}
		
		if(sb != null) 
		{
			restaurantesArray = new JSONArray(sb.toString());
			return true;
		}
		
		return false;
	}

	public static List<Restaurante> getRestaurantes()
	{
		ArrayList<Restaurante> restaurantes = new ArrayList<Restaurante>();
		
		if(parsearJSON(new File(ficheroJson))) 
		{
			for(int i = 0; i < restaurantesArray.length(); i++) 
			{
				Restaurante restaurante = new Restaurante();
				JSONObject restauranteJson = restaurantesArray.getJSONObject(i);
				
				String cif = restauranteJson.getString("cif");
				restaurante.setCif(cif);
				
				String nombre = restauranteJson.getString("nombre");
				restaurante.setNombre(nombre);
				
				String telefono = restauranteJson.getString("telefono");
				restaurante.setTelefono(telefono);
				
				String descripcion = restauranteJson.getString("descripcion");
				restaurante.setDescripcion(descripcion);
				
				JSONArray reservasJson = restauranteJson.getJSONArray("reservas");
				ArrayList<Reserva> reservas = new ArrayList<>();
				
				for(int j = 0; j < reservasJson.length(); j++) 
				{
					Reserva reserva = new Reserva();
					JSONObject reservaJson = reservasJson.getJSONObject(j);
					
					String descripcion_reserva = reservaJson.getString("descripcion");
					reserva.setDescripcion(descripcion_reserva);
					
					String email_resreva = reservaJson.getString("email");
					reserva.setEmail(email_resreva);
					
					LocalDate fecha_reserva = LocalDate.parse(reservaJson.getString("fecha"));
					reserva.setFecha(fecha_reserva);
					
					Double precio_reserva = reservaJson.getDouble("precio");
					reserva.setPrecio(precio_reserva);
					
					JSONObject clienteJson = reservaJson.getJSONObject("cliente");
					Cliente cliente = new Cliente();
					
					String nombre_cliente = clienteJson.getString("nombre");
					cliente.setNombre(nombre_cliente);
					
					String apellido_cliente = clienteJson.getString("apellidos");
					cliente.setApellidos(apellido_cliente);
					
					reserva.setCliente(cliente);
					
					reservas.add(reserva);
				}	
				
				restaurante.setReservas(reservas);
				
				JSONArray trabajadoresJson = restauranteJson.getJSONArray("trabajadores");
				ArrayList<Trabajador> trabajadores = new ArrayList<>();
				
				for(int k = 0; k < trabajadoresJson.length(); k++) 
				{
					Trabajador trabajador = new Trabajador();
					JSONObject trabajadorJson = trabajadoresJson.getJSONObject(k);
					
					String dni_trabajador = trabajadorJson.getString("dni");
					trabajador.setDni(dni_trabajador);
					
					String nombre_trabajador = trabajadorJson.getString("nombre");
					trabajador.setNombre(nombre_trabajador);;
					
					String apellidos_trabajador = trabajadorJson.getString("apellidos");
					trabajador.setApellidos(apellidos_trabajador);;
					
					String telefono_trabajador = trabajadorJson.getString("telefono");
					trabajador.setTelefono(telefono_trabajador);;
					
					String email_trabajador = trabajadorJson.getString("email");
					trabajador.setEmail(email_trabajador);
					
					trabajadores.add(trabajador);
				}
				
				restaurante.setTrabajadores(trabajadores);
				
				JSONArray menuJson = restauranteJson.getJSONArray("menu");
				ArrayList<Comida> menu = new ArrayList<Comida>();
				
				for(int l = 0; l < menuJson.length(); l++) 
				{
					Comida comida = new Comida();
					JSONObject comidaJson = menuJson.getJSONObject(l);
					
					String nombre_plato = comidaJson.getString("nombre");
					comida.setNombre(nombre_plato);
					
					TipoComida tipo = TipoComida.valueOf(comidaJson.getString("tipo"));
					comida.setTipo(tipo);
					
					menu.add(comida);
				}
				
				restaurante.setMenu(menu);
				
				restaurantes.add(restaurante);
			}
		}
		
		return restaurantes;
	}
}
