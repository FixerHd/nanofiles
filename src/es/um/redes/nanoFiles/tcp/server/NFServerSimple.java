package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;

	public NFServerSimple() throws IOException {
		/*
		 * TODO: Crear una direción de socket a partir del puerto especificado
		 */
		InetSocketAddress direccionSocket = new InetSocketAddress(PORT);
		
		
		/*
		 * TODO: Crear un socket servidor y ligarlo a la dirección de socket anterior
		 */

		serverSocket = new ServerSocket();
		serverSocket.bind(direccionSocket);

	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * 
	 */
	public void run() {
		/*
		 * TODO: Comprobar que el socket servidor está creado y ligado
		 */
		
		if (serverSocket.isBound()) {
		    System.out.println("El socket del servidor está ligado correctamente.");
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
		/*
		 * TODO: Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		
		
		/*
		 * TODO: Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */
		
		
		
		
		

		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
}
