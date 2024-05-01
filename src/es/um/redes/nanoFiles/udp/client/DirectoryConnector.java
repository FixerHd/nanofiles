package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 100000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;

	public DirectoryConnector(String address) throws IOException {	
		
		directoryAddress=new InetSocketAddress(InetAddress.getByName(address), DIRECTORY_PORT);		
		
		socket=new DatagramSocket();
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * @throws IOException 
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) throws IOException {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		
		DatagramPacket packet = new DatagramPacket(requestData, requestData.length, directoryAddress);
		socket.send(packet);
		int intentos = 0;
		while (intentos < MAX_NUMBER_OF_ATTEMPTS){
			try{
				socket.setSoTimeout(TIMEOUT);
				DatagramPacket receivePacket = new DatagramPacket(responseData, responseData.length);
                socket.receive(receivePacket);
				response = new byte[receivePacket.getLength()];
                System.arraycopy(receivePacket.getData(), 0, response, 0, receivePacket.getLength());
				break;
			} catch (IOException e) {
				intentos++;
                System.err.println("Attempt " + intentos + ": " + e.getMessage());
			}
		}

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
	
		boolean success = false;
		String mensaje = "login";
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
		
		}
		if (recibidos != null) {
			success = true;
		}
		String str1 = new String(recibidos);
		if (str1.equals("loginok")) {
			System.out.println("El mensaje recibido ha sido 'loginok'");
		}
		



		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 */
	public boolean logIntoDirectory(String nickname) {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;	
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_LOGIN + ":" + nickname);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			
		}
		if(recibidos==null) {
			return success;
		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		
		String op = rcbd.getOperation();

		String val;
		
		if(op.equals("loginok") && recibidos!=null) {
			success = true;
			val = rcbd.getSessionkey();
			System.out.println("Se ha hecho login éxitosamente y la clave de sesión es " + val);
			sessionKey=Integer.parseInt(val);
		} else {
			System.err.println("El mensaje recibido no es 'loginok'");
			return success;

		}
		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public String[] getUserList() {
		String[] userlist = null;
		assert (sessionKey != INVALID_SESSION_KEY);
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_USERLIST + ":" + sessionKey);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			
		}
		if(recibidos==null) {
			return userlist;
		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		String op = rcbd.getOperation();
		if(op.equals("userlistok") && recibidos!=null) {
			System.out.println(rcbd.getNicks());
		}
		if(str.equals("userlist_failed:-1")){
			return userlist;
		}else {
			
		}
		



		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 */
	public boolean logoutFromDirectory() {
		assert (sessionKey != INVALID_SESSION_KEY);
		boolean success = false;
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_LOGOUT + ":" + sessionKey);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			
		}
		if(recibidos==null) {
			return success;
		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		String op = rcbd.getOperation();
		String val;
		if(op.equals("logoutok") && recibidos!=null) {
			success = true;
			val = rcbd.getSessionkey();
			System.out.println("El logout se ha completado con éxito y se ha eliminado la clave " + val);
			sessionKey=INVALID_SESSION_KEY;
		} else {
			System.err.println("El mensaje recibido no es 'logoutok'");
			return success;

		}


		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
		boolean success = false;
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_REGISTER + ":" + serverPort + "," + sessionKey);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			

		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		String op = rcbd.getOperation();
		String val;
		if(op.equals("registerok") && recibidos!=null) {
			success = true;
			val = rcbd.getSessionkey();
			System.out.println("Se ha registrado en el directorio que el usuario con sessionKey: " + val + 
					" está sirviendo archivos");
		} else {
			System.err.println("El mensaje recibido no es 'registerok'");
			return success;

		}
		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;

	    // Construir y enviar la solicitud de búsqueda al directorio
		DirMessage lookupRequest = null;
		String[] partes = this.directoryAddress.toString().split(":");
		if(partes[0].contains("localhost")) {
			lookupRequest = DirMessage.fromString(DirMessageOps.OPERATION_LOOKUP + ":" + nick + ":");	
		} else {
			lookupRequest = DirMessage.fromString(DirMessageOps.OPERATION_LOOKUP + ":" + nick + ":" + partes[0]);
		}
	    String cadena = lookupRequest.toString();
	    byte[] requestData = cadena.getBytes(); // Convierte el mensaje en bytes

	    // Envía la solicitud al directorio y recibe la respuesta
	    byte[] responseData = null;
	    try {
	        responseData = sendAndReceiveDatagrams(requestData);
	    } catch (IOException e) {
	        // Manejar cualquier excepción que pueda ocurrir durante la comunicación con el directorio
	        e.printStackTrace();
	    }

	    // Analizar la respuesta del directorio para obtener la dirección del servidor
	    if (responseData != null) {
	        String responseStr = new String(responseData);
	        String[] parts = responseStr.split(":");
	        if (parts.length == 3 && parts[0].equals("lookup")) {
            	String ip = parts[1];
                int port = Integer.parseInt(parts[2]);
                try {
                	if(ip.contains("unresolved")) {
                		String[] ips = ip.split("<");
                		ip = ips[0].substring(0, ips[0].length() - 1);
                		
                	}
                	InetAddress inetAddress = InetAddress.getByName(ip.substring(1));
                    serverAddr = new InetSocketAddress(inetAddress, port);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
	        } else {
	            System.err.println("Respuesta no válida del directorio para la operación de búsqueda.");
	        }
	    } else {
	        System.err.println("No se recibió respuesta del directorio para la operación de búsqueda.");
	    }

	    return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;
		StringBuilder filesDetails = new StringBuilder();

        // Iterar sobre cada FileInfo
        for (FileInfo fileInfo : files) {
            // Concatenar los detalles de cada FileInfo al StringBuilder
            filesDetails.append("[")
            		   .append(fileInfo.fileName).append(":")
                       .append(fileInfo.fileHash).append(",") 
                       .append(fileInfo.fileSize).append(";")
                       .append(fileInfo.filePath).append("]").append("$"); 
        }
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_PUBLISH + ":" + filesDetails + "&" + sessionKey);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			

		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		String op = rcbd.getOperation();
		String val;
		if(op.equals("publishok") && recibidos!=null) {
			success = true;
			val = rcbd.getSessionkey();
			System.out.println("Los ficheros del usuario con sessionKey: " + val + 
					" se han publicado al directorio.");
		} else {
			System.err.println("El mensaje recibido no es 'publishok'");
			return success;

		}
		


		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = null;
		assert (sessionKey != INVALID_SESSION_KEY);
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_FILELIST + ":" + sessionKey);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			
		}
		if(recibidos==null) {
			return filelist;
		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		String op = rcbd.getOperation();
		int i = 0;
		if(op.equals("filelistok") && recibidos!=null) {
			String contenido = rcbd.getNicks();
			if(!contenido.equals("filelist_failed:-1")) {
				String[] archivosUser = contenido.split("\\$");
				filelist = new FileInfo[archivosUser.length];
				for(String s: archivosUser) {
					String[] atributos = s.split(":");
					FileInfo f = new FileInfo(atributos[1], atributos[0], Long.parseLong(atributos[2]), atributos[3]);
					filelist[i] = f;
					i++;
				}	
			}else {
				return filelist;
			}
	
		}
		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		assert (sessionKey != INVALID_SESSION_KEY);
		DirMessage msg = DirMessage.fromString(DirMessageOps.OPERATION_SEARCH + ":" + fileHash);
		String mensaje = msg.toString();
		byte[] datos = mensaje.getBytes();
		byte[] recibidos = null;
		try {
			recibidos = sendAndReceiveDatagrams(datos);
		} catch (IOException e) {
			
		}
		if(recibidos==null) {
			return nicklist;
		}
		String str = new String(recibidos);
		DirMessage rcbd = DirMessage.fromString(str);
		String op = rcbd.getOperation();
		int i = 0;
		if(op.equals("searchok") && recibidos!=null) {
			String contenido = rcbd.getNicks();
			String[] claves = contenido.split(":");
			nicklist = new String[claves.length];
			for(String s: claves) {
				nicklist[i] = s;
				i++;
			}
			
		}
		if(str.equals("searchok_fail")){
			return nicklist;
		}else {
			
		}

		return nicklist;
		
	}





}
