package es.aad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.restaurantes.Restaurante;
import es.restaurantes.Trabajador;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class Manager {
	private final static Logger LOG = LoggerFactory.getLogger(Manager.class);
	private final static String NOM_TABLA = "Restaurantes";
	private final static String KEY = "ASIAW5AXCLFPUL2VKTQZ";
	private final static String SECRET_KEY = "47MsIo0a0WRcMfuS/8SvteNqj2huNxIrfAXf9IZF";
	private final static String SESSION_TOKEN = "IQoJb3JpZ2luX2VjECAaCXVzLXdlc3QtMiJGMEQCIF3VTmsOlCUleqsOkDOasd5Qr7FWlp/YnYtO2XCshzpRAiALF/EEuOuRDAL07Xz2YhBVWRZFv+1"
			+ "/aCMHDT781SkMPSqmAghJEAAaDDQ3NDY0MjI3NDY1NSIM7cCTi1Lbs4Hde9GaKoMCbmeZ7O4gqH+jvI0TFpVrUPQ9khpxXvPqKYKfh8h3kDVFgjwApflpiHp9N5ldb/EZguOMU20j81MjnWtIVQIGuFyv"
			+ "6owIUxdzeDzNJwJcUZ7u/IHXstgvaKyu94mceAbCyfeEEczT8pINCrfYznJTbhlkOql2GRyvXn0/hRQeqoHHqVRR3w11pANjha/Pw+7hiVsgT4yV9zlb/isGMfK824GpnX5gqFBBf9GXEIOUgHylKWPD5"
			+ "r8ociNb+1Cgu48PHubGf/m4KZMeC4DqWOuXToDlc03+V2Y0TAoR3DKp6GTStjMaOMO3vuhWWSpZg1EZoqcwI7naY2E3imollhN1Mn/orzCS7cK9BjqeATgZvFFz9NDuBO4kYMNo8K8oONwI0r7IIZhUuOH"
			+ "7yS+ZVAmwcOIzgjejmlF0BjVfWX5AwOdp3pWMX8FF8qWnsseQAEbZEj4Ml7QOSsX4MUviVutO22SfUynzajgD1kzRGPul5dR7D0u5tSgLEk0/ocS2kPKOIPfOQDJ2ehTCkTA2Q3oJG88QXL2pDwf5XOSzQ"
			+ "eQBn6PWC9Qa9tbRQ0DI";
	private DynamoDbEnhancedClient cliente;
	private DynamoDbTable<Restaurante> tablaRestaurante;
	
	public Manager() {
		LOG.info("Se establece conexion con DynamoDB");
		cliente = DynamoDbEnhancedClient.builder().dynamoDbClient(client()).build();
		tablaRestaurante = cliente.table(NOM_TABLA, TableSchema.fromBean(Restaurante.class));
		LOG.info("Se ha establecido la conexion con exito.");
	}
	public static DynamoDbClient client() {
		AwsSessionCredentials credentials = AwsSessionCredentials.create(KEY, SECRET_KEY, SESSION_TOKEN);
		
		return DynamoDbClient.builder().region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.build();
	}
	
	/**
	 * 
	 * @param restaurante
	 */
	public void insertarRestaurante(Restaurante restaurante) {
		LOG.info("Se va a insertar el restaurante con CIF: [" + restaurante.getCif() + "]");
		tablaRestaurante.putItem(restaurante);
		LOG.info("Se ha insertado con exito el restaurante");
	}

	/**
	 * 
	 * @param CIF
	 */
	public Restaurante getRestaurante(String CIF) {
		Restaurante buscado = tablaRestaurante.getItem(Key.builder().partitionValue(AttributeValue.fromN(CIF)).build());
		
		if(buscado != null) {
			LOG.debug("Se ha encontrado el restaurante [" + buscado.getNombre() + "].");
		} else {
			LOG.warn("El restaurante con CIF: [" + CIF + "] no ha sido encontrado.");
		}
		return buscado;
	}
	
	/*
	public void borrarTrabajador(Trabajador trabajador, String CIF) {
		Restaurante buscado = getRestaurante(CIF);
				
		for(Trabajador t : buscado.getTrabajadores()) {
			if(t.equals(trabajador)) {
				tablaRestaurante.deleteItem(trabajador);
			}
		}
		
	}*/
}
