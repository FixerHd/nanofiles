package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NFServerSimple {

	//private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	//private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;
	private int puerto_dinamico = PORT;


	public NFServerSimple() throws IOException {

		boolean puertoDisponible = false;
        while (!puertoDisponible) {
            try {
                // Crear una dirección de socket a partir del puerto actual
                InetSocketAddress socketAddress = new InetSocketAddress(puerto_dinamico);
                // Crear un socket servidor y ligarlo a la dirección de socket
                serverSocket = new ServerSocket();
                serverSocket.bind(socketAddress);
                // Si no hay excepciones, el puerto está disponible
                puertoDisponible = true;
            } catch (IOException e) {
                // Si ocurre una excepción, el puerto está ocupado, intentar con el siguiente
                puerto_dinamico++;
            }
        }
		
		

	}
	
	public int getPuertodinamico() {
		return puerto_dinamico;
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * 
	 */
	public void run() {
		
		if (serverSocket.isBound()) {
		    System.out.println("El socket del servidor está ligado correctamente.");
		    System.out.println("PUERTO: " + puerto_dinamico);
		    while (true) {
			    try {
				    System.out.println("Esperando conexiones...");
				    Socket clientSocket = serverSocket.accept();
				    System.out.println("Un peer se ha conectado: " + clientSocket.getInetAddress());
				    NFServerComm.serveFilesToClient(clientSocket);
				} catch (IOException e) {
				    System.err.println("Error al aceptar la conexión del cliente: " + e.getMessage());
				}
		    }
		} else {
		    System.err.println("El socket del servidor no está ligado.");
		}
		
		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
}
