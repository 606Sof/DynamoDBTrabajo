package es.aad;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.restaurantes.Reserva;
import es.restaurantes.Restaurante;
import es.restaurantes.Trabajador;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
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
	 * @param CIF
	 */
	public void mostrarTrabajadoresRestaurante(String CIF) {
		Restaurante r = getRestaurante(CIF);
		LOG.info("Informacion del restaurante con CIF " + CIF + " \n"  + r.toString());
		LOG.info("Los trabajadores de este restaurante son ");
		int contador = 1;
		for (Trabajador t : r.getTrabajadores()) {	
			LOG.info("Trabajador " + contador + "\n " + t.toString());
			contador++;
		}
	}
	
	/**
	 * 
	 * @param CIF
	 */
	public void borrarRestaurante(String CIF) {
		Restaurante buscado = getRestaurante(CIF);
		if(buscado!=null) {
			tablaRestaurante.deleteItem(Key.builder().partitionValue(AttributeValue.fromS(CIF)).build());
			LOG.info("Se ha eliminado el restaurante con CIF["+CIF+"]");
		}else {
			LOG.warn("No existe restaurante con el CIF["+CIF+"]");
		}
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
	/**
	 * 
	 * @param CIF
	 * @param reserva
	 */
	public void insertarReserva(String CIF, Reserva reserva) {
		Restaurante buscado = getRestaurante(CIF);
		if(buscado!=null) {
			buscado.getReservas().add(reserva);
			tablaRestaurante.updateItem(buscado);
			LOG.info("Se ha añadido la reserva correctamente");
		}else {
			LOG.warn("No se ha encontrado el restaurante con CIF["+CIF+"]");
		}
	}
	/**
	 * 
	 * @param CIF
	 * @param email
	 */
	public boolean borrarReserva(String CIF, String email) {
		Restaurante buscado = getRestaurante(CIF);
		if(buscado!=null) {
			Iterator<Reserva> i = buscado.getReservas().iterator();
			while(i.hasNext()) {
				Reserva r = i.next();
				
				if(r.getEmail().equals(email)) {
					i.remove();
					tablaRestaurante.updateItem(buscado);
					LOG.info("Se ha borrado correctamente la reserva con el email["+email+"]");
					return true;
				}
			}
		}else {
			LOG.warn("No se ha encontrado el restaurante con CIF["+CIF+"]");
		}
		return false;
	}
	
	public void insertTrans(List<Restaurante> restaurantes) 
	{
		TransactWriteItemsEnhancedRequest request =  crearRequest(restaurantes);	    
	    	cliente.transactWriteItems(request);    
	}
	
	public TransactWriteItemsEnhancedRequest crearRequest(List<Restaurante> restaurantes) 
	{
		TransactWriteItemsEnhancedRequest.Builder request = TransactWriteItemsEnhancedRequest.builder();
		for(Restaurante r : restaurantes)
			request.addPutItem(tablaRestaurante, r);	
		return request.build();
	}
}
