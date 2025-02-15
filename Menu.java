package operacionesDynamoDB;

import java.time.LocalDate;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Menu {
	private static Logger LOG = LoggerFactory.getLogger(Menu.class);
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		ConexDynamoDB cd = new ConexDynamoDB();
		int operacion;
		do {

			menu();
			operacion = comprobarInt(sc);
			comprobarOpcion(operacion);
			switch (operacion) {
			case 1:
				LOG.info("Dime el nombre del restaurante en el que esta el trabajador   ");
				String nombreRestaurante = comprobarString(sc);
				//metodo para buscar el resturante
				
				LOG.info("Dime el dni del trabajador que quieres modificar    ");
				String dniTrabajador = comprobarString(sc);
				//metodo para buscar el trabajador
				
				LOG.info("Dime el teléfono nuevo del trabajador con DNI " + dniTrabajador);
				String telefonoTrabajador = comprobarString(sc);
				//metodo para modificar el telefono
				cd.updateItem(dniTrabajador, telefonoTrabajador);
				break;
			case 2:
				LOG.info("Dime la fecha de la reserva en formato yy/MM/dd   ");
				String fechaString = comprobarString(sc);
				LocalDate fecha = LocalDate.parse(fechaString);
				LOG.info("Dime el telefono del cliente");
				String telefono = comprobarString(sc);
				LOG.info("Dime el nombre del cliente que hace la reserva   ");
				String nombreCli = comprobarString(sc);
				LOG.info("Dime los apellidos del cliente que hace la reserva   ");
				String apellidosCli = comprobarString(sc);
				Reserva r = new Reserva(fecha, telefono);
				Cliente cli = new Cliente(nombreCli, apellidoCli, r);
				//metodo para insertar una reserva
				cd.insertItem(r);
				cd.insertItem(cli);
				break;
			case 3:
				LOG.info("Dime el nombre del restaurante que quieres eliminar    ");
				String nombreRestaurant = comprobarString(sc);
				//metodo para eliminar un restauratne por nombre
				//primero buscar el restaurante por nombre y luego por id borrarlo
				//cd.deleteItem(nombreRestaurant);
				break;
			case 4:
				LOG.info("Dime el nombre del restaurante   ");
				String nombreRest = comprobarString(sc);
				//metodo para mostrar la info de un restaurante y sus trabajadores
				cd.retrieveItem(nombreRest);
				}
		} while (operacion != 0);
		LOG.info("Saliste del programa");
	}

	public static void menu() {
		LOG.info("Qué operación quieres hacer ?");
		LOG.info("1. Modificar telefono de trabajador");
		LOG.info("2. Añadir una reserva");
		LOG.info("3. Eliminar un restaurante");
		LOG.info("4. Obtener informacion de un restaurante o de los trabajadores");
		LOG.info("0. Salir del programa");

	}

	public static void comprobarOpcion(int opcion) {
		// comprobamos que la opcion es una de las posibles del menu
		while ((!(opcion == 1) && !(opcion == 2) && !(opcion == 3) && !(opcion == 0) && !(opcion == 4) && !(opcion == 5))) {
			LOG.info("Por favor indique una de las opciones posibles\n");
			break;
		}
	}

	// metodo para comprobar que el dato introducido por el usuario es correcto
	public static int comprobarInt(Scanner sc) {
		int numero = 0;
		boolean numeroIntroducido = false; // inicializamos en falso la variable numeroIntroducido
		// mientras sea falso
		while (!numeroIntroducido) {
			// si es un numero entero
			if (sc.hasNextInt()) {
				// asignamos el numemero entero a la variable
				numero = sc.nextInt();
				numeroIntroducido = true; // y hacemos que sea true para uqe salga del bucle
			} else {
				// sino le seguiremos preguntando un numero valido
				LOG.info("Eso no es un numero entero");
				sc.next();
			}
		}
		return numero; 
	}
	
	public static String comprobarString(Scanner sc) {
	    String texto = "";
	    boolean textoValido = false;
	    while (!textoValido) {
	        texto = sc.nextLine().trim(); 
	        if (!texto.isEmpty()) {
	            textoValido = true; 
	        } else {
	        	LOG.info("Entrada no válida. Por favor, introduce un texto válido:  ");
	        }
	    }
	    return texto;
	}
}
