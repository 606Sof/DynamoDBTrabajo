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
	private final static String KEY = "ASIAW5AXCLFPSEKL4BES";
	private final static String SECRET_KEY = "SV8RSzk02NC4FU46wtAeH+eESHMnVzGsJGrHCMDC";
	private final static String SESSION_TOKEN = "";
			private DynamoDbEnhancedClient cliente;
	private DynamoDbTable<Restaurante> tablaRestaurante;
	
	public Manager() {
		LOG.info("Se establece conexion con DynamoDB");
		cliente = DynamoDbEnhancedClient.builder().dynamoDbClient(client()).build();
		tablaRestaurante = cliente.table(NOM_TABLA, TableSchema.fromBean(Restaurante.class));
		LOG.info("Se ha establecido la conexion con exito.");
	}
	
	public static DynamoDbClient client() {
		LOG.debug("Estableciendo conexion, creando cliente");
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
		Restaurante buscado = tablaRestaurante.getItem(Key.builder().partitionValue(AttributeValue.fromS(CIF)).build());
		
		if(buscado != null) {
			LOG.debug("Se ha encontrado el restaurante [" + buscado.getNombre() + "].");
		} else {
			LOG.warn("El restaurante con CIF: [" + CIF + "] no ha sido encontrado.");
		}
		return buscado;
	}
	/**
	 * 
	 * @param trabajador
	 * @param CIF
	 */
	public void borrarTrabajador(Trabajador trabajador, String CIF) {
		Restaurante buscado = getRestaurante(CIF);
		if(buscado.getTrabajadores().remove(trabajador)) {
			LOG.info("Se va a borrar el trabajador con DNI: [" + trabajador.getDni() + "]");
		} else {
			LOG.warn("No se ha encontrado el trabajador a borrar");
		}
		tablaRestaurante.updateItem(buscado);
	}
	/**
	 * 
	 * @param telefono
	 * @param CIF
	 * @param dni
	 * @return
	 */
	public boolean actualizarTrabajador(String telefono, String CIF, String dni) {
	    Restaurante buscado = getRestaurante(CIF);
	    if(buscado!=null) {
	    	for(Trabajador t : buscado.getTrabajadores()) {
	    		if(t.getDni().equals(dni)) {
	    			t.setTelefono(telefono);
	    			tablaRestaurante.updateItem(buscado);
	    			return true;
	    		}
	    	}
	    }else {
	    	LOG.warn("No se ha encontrado el restaurante con CIF["+CIF+"]");
	    }
	    return false;
	}
}
