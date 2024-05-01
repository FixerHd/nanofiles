package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	
	private HashMap<String, InetSocketAddress> IPpuertos;

	private HashMap<Integer, FileInfo[]> ficheros;


	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		
		socket = new DatagramSocket(DIRECTORY_PORT);
		
		
		nicks = new HashMap<>();
		sessionKeys = new HashMap<>();
		IPpuertos = new HashMap<>();
		ficheros = new HashMap<>();


		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		
		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			byte[] receptionBuffer = new byte[65507];
			InetSocketAddress clientAddr = null;
			int dataLength = -1;
			
			DatagramPacket paqueteDeCliente = new DatagramPacket(receptionBuffer, receptionBuffer.length);
			
			socket.receive(paqueteDeCliente);
			
			dataLength=paqueteDeCliente.getLength();
			
			clientAddr=(InetSocketAddress) paqueteDeCliente.getSocketAddress();



			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String messageFromClient = null;
				
				messageFromClient = new String(receptionBuffer, 0, paqueteDeCliente.getLength());



				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					
					if (messageFromClient.equals("login")) {
						String messageToClient = "loginok";
						byte[] dataToClient = messageToClient.getBytes();
						DatagramPacket paqueteACliente = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
						socket.send(paqueteACliente);
					} else {
						System.err.println("Error. El mensaje recibido no es 'login'");
						break;
					}


				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
				
					String datos = new String(receptionBuffer);
					DirMessage cadena = DirMessage.fromString(datos);
					DirMessage mensaje = buildResponseFromRequest(cadena, clientAddr);
					byte[] campo1 = mensaje.toString().getBytes();
					int campo2 = mensaje.toString().getBytes().length;
					DatagramPacket paqueteEnviar = new DatagramPacket(campo1, campo2, clientAddr); 
					socket.send(paqueteEnviar);
						

				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		
		String operation = msg.getOperation();

		DirMessage response = null;




		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();
			int sessionkey = -1;
			String mensajeACliente;
	
			if(!nicks.keySet().contains(username)) {
				
				sessionkey = random.nextInt(10000);
				nicks.put(username, sessionkey);
				sessionKeys.put(sessionkey, username);
				mensajeACliente = "loginok:" + sessionkey;
				response = DirMessage.fromString(mensajeACliente);
			} else {
				mensajeACliente = "login_failed:-1";
				response = DirMessage.fromString(mensajeACliente);
			}

			break;
		}
		
		case DirMessageOps.OPERATION_USERLIST: {
			String mensajeACliente = "";
			Set <String> conjunto_nicks = new HashSet<>();
			if(nicks.isEmpty()) {
				mensajeACliente = "userlist_failed:-1";
				response = DirMessage.fromString(DirMessageOps.OPERATION_USERLISTOK + ":" + mensajeACliente);
			}else {
				conjunto_nicks = nicks.keySet();
				for(String s : conjunto_nicks) {
					mensajeACliente += s;
					if(IPpuertos.keySet().contains(s)) {
						mensajeACliente += " 'Sirviendo ficheros...'";
					}
					mensajeACliente += "\n";
				}
				response = DirMessage.fromString(DirMessageOps.OPERATION_USERLISTOK + ":" + mensajeACliente);
			}
			break;
		}
		
		case DirMessageOps.OPERATION_LOGOUT: {
			System.out.println("En el mapa de nicks: " + nicks.keySet().size());
			String sessionKey = msg.getSessionkey();
			String username = msg.getNickname();
			nicks.remove(username);
			 Iterator<HashMap.Entry<String, Integer>> iter = nicks.entrySet().iterator();
		        while (iter.hasNext()) {
		            HashMap.Entry<String, Integer> entry = iter.next();
		            if (entry.getValue().equals(Integer.parseInt(sessionKey))) {
		                iter.remove();
		                System.out.println("Se eliminó la entrada con clave '" + entry.getKey() + "' y valor '" + entry.getValue() + "'");
		            }
		        }
	        System.out.println("En el mapa de nicks: " + nicks.keySet().size());
			response = DirMessage.fromString(DirMessageOps.OPERATION_LOGOUTOK + ":" + sessionKey);
			
			break;
		}case DirMessageOps.OPERATION_LOOKUP: {
			String nickname = msg.getNickname();
			String server = msg.getServer();
			if(server==null) {
				response = DirMessage.fromString(DirMessageOps.OPERATION_LOOKUP +
						":" + IPpuertos.get(nickname) + ":" + "");
			}else {
				InetSocketAddress aux = new InetSocketAddress(server, IPpuertos.get(nickname).getPort());
				response = DirMessage.fromString(DirMessageOps.OPERATION_LOOKUP +
						":" + aux + ":" + "");	
			}
			
			
			break;
		}case DirMessageOps.OPERATION_REGISTER: {
			String nickname = msg.getNickname();
			String sessionKey = msg.getSessionkey();
			String usuario = sessionKeys.get(Integer.parseInt(sessionKey));
			InetSocketAddress ip = new InetSocketAddress(clientAddr.getAddress(), Integer.parseInt(nickname));
			IPpuertos.put(usuario, ip);
			response = DirMessage.fromString(DirMessageOps.OPERATION_REGISTEROK + ":" + sessionKey);
			break;
		}case DirMessageOps.OPERATION_PUBLISH: {
			
			String nickname = msg.getNickname();
			int sessionKey = Integer.parseInt(msg.getSessionkey());
	        String[] arrayDeStrings = nickname.replace("[", "").replace("]", "").split("\\$");
	        FileInfo[] archivos = new FileInfo[arrayDeStrings.length];
	        int i = 0;
			for (String s : arrayDeStrings) {
	            // Quitamos los corchetes y separamos AQUI string por los dos puntos y el punto y coma
	            String[] atributos = s.replaceAll("[\\[\\]]", "").split("[:;,]");
	            FileInfo fileInfo = new FileInfo(atributos[1], atributos[0], Integer.parseInt(atributos[2]), atributos[3]);
	            archivos[i]=fileInfo;
	            i++;
	        }
			ficheros.put(sessionKey, archivos);
			response = DirMessage.fromString(DirMessageOps.OPERATION_PUBLISHOK + ":" + sessionKey);
			break;
		}case DirMessageOps.OPERATION_FILELIST: {
			String mensajeACliente = "";
			Set <Integer> conjunto_keys = new HashSet<>();
			if(ficheros.isEmpty()) {
				mensajeACliente = "filelist_failed:-1";
				response = DirMessage.fromString(DirMessageOps.OPERATION_FILELISTOK + ":" + mensajeACliente);
			}else {
				conjunto_keys = ficheros.keySet();
				for(Integer i : conjunto_keys) {
					FileInfo[] arrayFicheros = ficheros.get(i);
					for(FileInfo file: arrayFicheros) {
						mensajeACliente			 
					             += file.fileName + ":"
					             + file.fileHash + ":"
					             + file.fileSize + ":"
					             + file.filePath + "$";
					}
					
				}
				response = DirMessage.fromString(DirMessageOps.OPERATION_FILELISTOK + ":" + mensajeACliente);
			}
			break;
		}case DirMessageOps.OPERATION_SEARCH: {
			String mensajeACliente = "";
			Set <Integer> conjunto_keys = ficheros.keySet();
			String[] claves = new String[conjunto_keys.size()];
			String subhash = msg.getNickname();
			int i = 0;
			if(ficheros.isEmpty()) {
				mensajeACliente = "search_failed";
				response = new DirMessage(mensajeACliente);
			}else {
				conjunto_keys = ficheros.keySet();
				for(Integer key : conjunto_keys) {
					FileInfo[] arrayFicheros = ficheros.get(key);
					for(FileInfo file: arrayFicheros) {
						if(file.fileHash.contains(subhash)) {
							claves[i]=sessionKeys.get(key);
							i++;
						}
						
					}
					
				}
				for(String j: claves) {
					mensajeACliente			 
		             += j + ":";
				}
				response = DirMessage.fromString(DirMessageOps.OPERATION_SEARCHOK + ":" + mensajeACliente);
			}
			break;
		}
		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
		}
		return response;

	}
}
