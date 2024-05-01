package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;


import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {

	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para comprobar que la comunicación con el directorio es exitosa (se
	 * pueden enviar y recibir datagramas) haciendo uso de la clase
	 * DirectoryConnector
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected void testCommunicationWithDirectory(String directoryHostname) throws IOException {
		assert (NanoFiles.testMode);
		System.out.println("[testMode] Testing communication with directory...");
		/*
		 * Crea un objeto DirectoryConnector a partir del parámetro directoryHostname y
		 * lo utiliza para hacer una prueba de comunicación con el directorio.
		 */
		DirectoryConnector directoryConnector = new DirectoryConnector(directoryHostname);
		if (directoryConnector.testSendAndReceive()) {
			System.out.println("[testMode] Test PASSED!");
		} else {
			System.err.println("[testMode] Test FAILED!");
		}
	}

	/**
	 * Método para conectar con el directorio y obtener la "sessionKey" que se
	 * deberá utilizar en lo sucesivo para identificar a este cliente ante el
	 * directorio
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected boolean doLogin(String directoryHostname, String nickname) {

		
		boolean result = false;

		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = directoryConnector.logIntoDirectory(nickname);
		if(!result) directoryConnector = null;
		return result;
	}

	/**
	 * Método para desconectarse del directorio: cerrar sesión y dar de baja el
	 * nombre de usuario registrado
	 */
	public boolean doLogout() {
		
		boolean result = false;
		result = directoryConnector.logoutFromDirectory();
		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 */
	protected boolean getAndPrintUserList() {
		
		boolean result = false;
		String[] array = directoryConnector.getUserList();
		if(array == null) {
			return result;
			
		}
		result = true;
		
		for(int i = 0; i<array.length; i++) {
			System.out.println(array[i]);
		}
		
		return result;

	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	protected boolean getAndPrintFileList() {
		
		boolean result = false;
		FileInfo[] array = directoryConnector.getFileList();
		if(array == null) {
			System.err.println("Aún no hay ficheros publicados en el directorio.");
			return result;
		}
		result = true;
		FileInfo.printToSysout(array);


		return result;
	}

	/**
	 * Método para registrarse en el directorio como servidor de ficheros en un
	 * puerto determinado
	 * 
	 * @param serverPort el puerto en el que está escuchando nuestro servidor de
	 *                   ficheros
	 */

	public boolean registerFileServer(int serverPort) {
		
		boolean result = false;
		result = directoryConnector.registerServerPort(serverPort);	
	
		return result;
		
	}

	/**
	 * Método para enviar al directorio la lista de ficheros que este peer servidor
	 * comparte con el resto (ver método filelist).
	 * 
	 */
	protected boolean publishLocalFiles() {
		
		boolean result = false;
		FileInfo[] ficheros = NanoFiles.db.getFiles();
		result = directoryConnector.publishLocalFiles(ficheros);	
	
		return result;
	}

	/**
	 * Método para consultar al directorio el nick de un peer servidor y obtener
	 * como respuesta la dirección de socket IP:puerto asociada a dicho servidor
	 * 
	 * @param nickname el nick del servidor por cuya IP:puerto se pregunta
	 * @return La dirección de socket del servidor identificado por dich nick, o
	 *         null si no se encuentra ningún usuario con ese nick que esté
	 *         sirviendo ficheros.
	 */
	private InetSocketAddress lookupServerAddrByUsername(String nickname) {
		
		InetSocketAddress serverAddr = null;
	    serverAddr = directoryConnector.lookupServerAddrByUsername(nickname);
		return serverAddr;
	}

	/**
	 * Método para obtener la dirección de socket asociada a un servidor a partir de
	 * una cadena de caracteres que contenga: i) el nick del servidor, o ii)
	 * directamente una IP:puerto.
	 * 
	 * @param serverNicknameOrSocketAddr El nick o IP:puerto del servidor por el que
	 *                                   preguntamos
	 * @return La dirección de socket del peer identificado por dicho nick, o null
	 *         si no se encuentra ningún peer con ese nick.
	 */
	public InetSocketAddress getServerAddress(String serverNicknameOrSocketAddr) {
		InetSocketAddress fserverAddr = null;
		
		if (serverNicknameOrSocketAddr.contains(":")) { // Then it has to be a socket address (IP:port)
			
			 String[] parts = serverNicknameOrSocketAddr.split(":");
		        if (parts.length == 2) {
		            String ipAddress = parts[0];
		            int port = Integer.parseInt(parts[1]);
		            try {
		                InetAddress inetAddress = InetAddress.getByName(ipAddress);
		                fserverAddr = new InetSocketAddress(inetAddress, port);
		            } catch (UnknownHostException e) {
		                e.printStackTrace();
		            }
		        }


		} else {
			

			fserverAddr = lookupServerAddrByUsername(serverNicknameOrSocketAddr);
		}
		return fserverAddr;
	}

	/**
	 * Método para consultar al directorio los nicknames de los servidores que
	 * tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 */
	public boolean getAndPrintServersNicknamesSharingThisFile(String fileHashSubstring) {
		
		
		boolean result = false;

		String[] claves = directoryConnector.getServerNicknamesSharingThisFile(fileHashSubstring);
		if(claves == null) {
			return result;
			
		}
		result = true;

		if (claves.length > 0) {
		    StringBuilder mensaje = new StringBuilder("El fichero lo tienen los usuarios: ");
		    for (int i = 0; i < claves.length; i++) {
		        mensaje.append(claves[i]);
		        if (i < claves.length - 1) {
		            mensaje.append(", ");
		        }
		    }
		    System.out.println(mensaje.toString());
		} else {
		    System.out.println("El fichero no está compartido por ningún usuario.");
		}


		return result;
	}

	/**
	 * Método para consultar al directorio las direcciones de socket de los
	 * servidores que tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 * @return Una lista de direcciones de socket de los servidores que comparten
	 *         dicho fichero, o null si dicha subcadena del hash no identifica
	 *         ningún fichero concreto (no existe o es una subcadena ambigua)
	 * 
	 */
	public LinkedList<InetSocketAddress> getServerAddressesSharingThisFile(String downloadTargetFileHash) {
		LinkedList<InetSocketAddress> serverAddressList = null;
		




		return serverAddressList;
	}

	/**
	 * Método para dar de baja a nuestro servidor de ficheros en el directorio.
	 * 
	 * @return Éxito o fracaso de la operación
	 */
	public boolean unregisterFileServer() {
		
		boolean result = false;



		return result;
	}

	protected InetSocketAddress getDirectoryAddress() {
		return directoryConnector.getDirectoryAddress();
	}
	
	public boolean isnotLogged() {
		return directoryConnector == null;
	}

}
