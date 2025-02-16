package es.aad;

import java.time.LocalDate;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.restaurantes.Cliente;
import es.restaurantes.Reserva;
import es.restaurantes.Restaurante;

public class Menu {
	private static Logger LOG = LoggerFactory.getLogger(Menu.class);
	static Scanner sc = new Scanner(System.in);
	static Manager manager = new Manager();

	public static void main(String[] args) {

		int operacion;
		int operacionCase4;
		do {
			menu();
			operacion = comprobarInt(sc);
			comprobarOpcion(operacion);
			switch (operacion) {
			case 1:
				modificarTrabajador();
				break;
			case 2:
				insertarReserva();
				break;
			case 3:
				eliminarRestaurante();
				break;
			case 4:
				LOG.info("1. Informacion de un restaurante");
				LOG.info("2. Informacion de los trabajadores de un restaurante");
				operacionCase4 = comprobarInt(sc);
				switch(operacionCase4) {
				case 1: 
						buscarRestaurante();
						break;
						
				case 2:
						buscarTrabajadores();
						break;		
				default:
					LOG.warn("Elija una de las opciones posibles (1 o 2)");
				}
				break;
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
		while ((!(opcion == 1) && !(opcion == 2) && !(opcion == 3) && !(opcion == 0) && !(opcion == 4)
				&& !(opcion == 5))) {
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

	public static void modificarTrabajador() {
		LOG.info("Dime el CIF del restaurante en el que esta el trabajador   ");
		String cifRestaurante = comprobarString(sc);

		LOG.info("Dime el dni del trabajador que quieres modificar    ");
		String dniTrabajador = comprobarString(sc);

		LOG.info("Dime el teléfono nuevo del trabajador con DNI " + dniTrabajador);
		String telefonoTrabajador = comprobarString(sc);
		if (manager.actualizarTrabajador(telefonoTrabajador, cifRestaurante, dniTrabajador)) {
			LOG.info("Trabajador con DNI " + dniTrabajador + " actualizado correctamente");
		} else {
			LOG.warn("Error al actualizar al trabajador con DNI " + dniTrabajador);
		}
	}

	public static void insertarReserva() {
		LOG.info("Dime el CIF del restaurante   ");
		String cifRestaurante = comprobarString(sc);
		LOG.info("Dime la fecha de la reserva en formato yy/MM/dd   ");
		String fechaString = comprobarString(sc);
		LocalDate fecha = LocalDate.parse(fechaString);
		LOG.info("Dime el email del cliente");
		String email = comprobarString(sc);
		LOG.info("Dime el nombre del cliente que hace la reserva   ");
		String nombreCli = comprobarString(sc);
		LOG.info("Dime los apellidos del cliente que hace la reserva   ");
		String apellidosCli = comprobarString(sc);
		Reserva r = new Reserva(fecha, email);
		Cliente cli = new Cliente(nombreCli, apellidosCli, r);
		r.setCliente(cli);
		//metodo para insertar una reserva
		manager.insertarReserva(cifRestaurante, r);
	}

	public static void eliminarRestaurante() {
		LOG.info("Dime el CIF del restaurante que quieres eliminar    ");
		String cifRest = comprobarString(sc);
		manager.borrarRestaurante(cifRest);
	}

	public static void buscarRestaurante() {
		LOG.info("Dime el CIF del restaurante   ");
		String cifRestaurante = comprobarString(sc);
		Restaurante r = manager.getRestaurante(cifRestaurante);
		LOG.info("Informacion del restaurante con CIF " + cifRestaurante + " \n"  + r.toString());
	}
	
	public static void buscarTrabajadores() {
		LOG.info("Dime el CIF del restaurante   ");
		String cifRestaurante = comprobarString(sc);
		manager.mostrarTrabajadoresRestaurante(cifRestaurante);
	}
}
