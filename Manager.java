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

/**
 * Esta clase se encarga de establecer la conexion y hacer uso de las operaciones CRUD en DynamoDB con varias clases referenciando a una tabla
 * 
 * @author Jorge C, Sofia U, Angel Z y Jaime T
 */
public class Manager {
	private final static Logger LOG = LoggerFactory.getLogger(Manager.class);
	private final static String NOM_TABLA = "Restaurantes";
	private final static String KEY = "ASIAW5AXCLFP4J2TX54K";
	private final static String SECRET_KEY = "kzRkQwK1WMk09H9aYjr8B9YoWZT615/Z1F89bbq5";
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
	 * Este metodo hace una consulta a la base de datos de un restaurante por su CIF (PK de la BD) y lo devuelve en caso de encontrarlo, en caso contrario devuelve null
	 * @param CIF = cadena de texto referida a el CIF del Restaurante que se quiere buscar
	 * @return el restaurante encontrado o null en caso de no existir
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
	 * Este metodo vuelca al log los trabajadores de un restaurante pasado por parametro
	 * @param CIF = cadena de texto referida a el CIF del Restaurante que se quiere buscar para listar sus trabajadores
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
	 * Este metodo borra el restaurante que coincida con el CIF pasado por parametro en caso de existir
	 * @param CIF = cadena de texto referida a el CIF del Restaurante que se quiere borrar
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
	 * Este metodo actualiza el telefono de un trabajador de un restaurante especifico
	 * @param telefono = valor nuevo que se quiere dar al numero de telefono
	 * @param CIF = cadena de texto referida a el CIF del Restaurante 
	 * @param dni = cadena de texto referida al DNI del trabajador que se quiere modificar
	 * @return True - en caso de haber actualizado correctamente el trabajador y False - en caso de no haberse actualizado
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
	 * Este metodo inserta una instancia del objeto Reserva en un restaurante especificado por su CIF
	 * @param CIF = cadena de texto referida a el CIF del Restaurante
	 * @param reserva = instancia del objeto Reserva que se quiere insertar
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
	 * Este metodo inserta de forma transaccional los restaurantes de la lista pasada como parametro
	 * @param restaurantes = lista de instancias de Restaurante que se quiere insertar
	 */
	public void insertTrans(List<Restaurante> restaurantes) 
	{
		TransactWriteItemsEnhancedRequest request =  crearRequest(restaurantes);	    
	    	cliente.transactWriteItems(request);    
	}
	/**
	 * Crea una peticion (request) usada para la insercion transaccional de instancias
	 * @param restaurantes = lista de instancias de restaurantes que quiere ser insertada en la BD
	 * @return el objeto TransactWriteItemsEnhancedRequest con el que se hara la insercion transaccional
	 */
	public TransactWriteItemsEnhancedRequest crearRequest(List<Restaurante> restaurantes) 
	{
		TransactWriteItemsEnhancedRequest.Builder request = TransactWriteItemsEnhancedRequest.builder();
		for(Restaurante r : restaurantes)
			request.addPutItem(tablaRestaurante, r);
		return request.build();
	}
	
	/*-----------------------METODOS NO USADOS EN EL MAIN, PERO UTILES EN FUTUROS PROGRAMAS-------------------------------------*/
	
	/**
	 * Este metodo borra un trabajador de un restaurante pasado como parametro
	 * @param trabajador = instancia de trabajador que quiere ser borrado
	 * @param CIF = cadena de texto referida a el CIF del Restaurante del que se quiere borrar el trabajador
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
	 * Este metodo sirve para insertar un objeto Restaurante en la base de datos
	 * @param restaurante = instancia del objeto restaurante que va a ser insertado
	 */
	public void insertarRestaurante(Restaurante restaurante) {
		LOG.info("Se va a insertar el restaurante con CIF: [" + restaurante.getCif() + "]");
		tablaRestaurante.putItem(restaurante);
		LOG.info("Se ha insertado con exito el restaurante");
	}
	/**
	 * Este metodo borra una reserva en base a su email asociado 
	 * @param CIF = cadena de texto referida a el CIF del Restaurante
	 * @param email = cadena de texto referida al email de la reserva que se quiere borrar
	 * @return True - si se ha encontrado y borrado la reserva o False - si no se ha econtrado o borrado la reserva 
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
}
