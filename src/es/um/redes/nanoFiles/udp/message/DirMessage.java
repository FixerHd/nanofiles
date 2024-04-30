package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	//private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */



	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	
	private int sessionkey;
	
	private String nicks;
	
	private String server;
	
	
	



	public DirMessage(String op) {
		operation = op;
	}




	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {



		nickname = nick;
	}

	public String getNickname() {



		return nickname;
	}
	
	public void setSessionkey(String num) {



		sessionkey = Integer.parseInt(num);
	}

	public String getSessionkey() {



		return  Integer.toString(sessionkey);
	}
	
	public void setServer(String num) {



		server = num;
	}
	
	public String getServer() {


		return server;
	}
	
	



	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		DirMessage m = null;
		int idx = message.indexOf(DELIMITER); // Posición del delimitador
		String field = message.substring(0, idx).toLowerCase(); // minúsculas;
		String value = message.substring(idx + 1).trim();
		

			assert (m == null);
			switch(field) {
			
				case DirMessageOps.OPERATION_LOGIN:
				{
					m = new DirMessage(field);
					m.setNickname(value);
					break;
				}
				case DirMessageOps.OPERATION_LOGINOK:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}
				case DirMessageOps.OPERATION_LOGOUT:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}
				case DirMessageOps.OPERATION_USERLIST:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}
				case DirMessageOps.OPERATION_LOGOUTOK:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}
				case DirMessageOps.OPERATION_USERLISTOK:
				{
					m = new DirMessage(field);
					m.setNicks(value);
					
					break;
				}case DirMessageOps.OPERATION_LOGIN_FAILED:
				{
					m = new DirMessage(field);
					m.setNickname(value);
					break;
				}case DirMessageOps.OPERATION_LOOKUP:
				{
					m = new DirMessage(field);
					String[] partes = value.split(":");
					m.setNickname(partes[0]);
					if(partes.length==2) {
						m.setServer(partes[1]);
					}
					break;
				}case DirMessageOps.OPERATION_REGISTER:
				{
					m = new DirMessage(field);
					String[] cadenas = value.split(",");
					m.setNickname(cadenas[0]);
					m.setSessionkey(cadenas[1]);
					break;
				}case DirMessageOps.OPERATION_REGISTEROK:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}case DirMessageOps.OPERATION_PUBLISH:
				{
					m = new DirMessage(field);
					String[] partes = value.split("&");
					m.setSessionkey(partes[1]);
					m.setNickname(partes[0]);
					break;
				}case DirMessageOps.OPERATION_PUBLISHOK:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}case DirMessageOps.OPERATION_FILELIST:
				{
					m = new DirMessage(field);
					m.setSessionkey(value);
					break;
				}case DirMessageOps.OPERATION_FILELISTOK:
				{
					m = new DirMessage(field);
					m.setNicks(value);
					
					break;
				}case DirMessageOps.OPERATION_SEARCH:
				{
					m = new DirMessage(field);
					m.setNickname(value);
					break;
				}case DirMessageOps.OPERATION_SEARCHOK:
				{
					m = new DirMessage(field);
					m.setNicks(value);
					
					break;
				}
				default:
				{
					System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + field);
					System.err.println("Message was:\n" + message);
					m = new DirMessage(field);
					
				}
			}
	
		
		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		String s = null;
		switch(operation) {
			case DirMessageOps.OPERATION_LOGIN:
			{
				s = operation + ":" + nickname;
				break;
				
			}
			case DirMessageOps.OPERATION_LOGINOK:
			{
				s = operation + ":" + getSessionkey();
				break;
				
			}
			case DirMessageOps.OPERATION_LOGOUT:
			{
				s = operation + ":" + getSessionkey();
				break;
				
			}
			case DirMessageOps.OPERATION_USERLIST:
			{
				s = operation + ":" + getSessionkey();
				break;
				
			}
			case DirMessageOps.OPERATION_LOGOUTOK:
			{
				s = operation + ":" + getSessionkey();
				break;
				
			}
			case DirMessageOps.OPERATION_USERLISTOK:
			{
				s = operation + ":" + getNicks();
				break;
				
			}case DirMessageOps.OPERATION_LOGIN_FAILED:
			{
				s = operation + ":" + nickname;
				break;
				
			}case DirMessageOps.OPERATION_LOOKUP:
			{
				if(server==null) {
					s = operation + ":" + nickname;
					break;
				}
				s = operation + ":" + nickname + ":" + server;
				break;
				
			}case DirMessageOps.OPERATION_REGISTER:
			{
				s = operation + ":" + nickname + "," + sessionkey;
				break;
				
			}case DirMessageOps.OPERATION_REGISTEROK:
			{
				s = operation + ":" + sessionkey;
				break;
				
			}case DirMessageOps.OPERATION_PUBLISH:
			{
				s = operation + ":" + nickname + "&" + sessionkey;
				break;
			}case DirMessageOps.OPERATION_PUBLISHOK:
			{
				s = operation + ":" + sessionkey;
				break;
			}case DirMessageOps.OPERATION_FILELIST:
			{
				s = operation + ":" + getSessionkey();
				break;
			}case DirMessageOps.OPERATION_FILELISTOK:
			{
				s = operation + ":" + getNicks();
				break;
			}case DirMessageOps.OPERATION_SEARCH:
			{
				s = operation + ":" + getNickname();
				break;
			}case DirMessageOps.OPERATION_SEARCHOK:
			{
				s = operation + ":" + getNicks();
				break;
			}
			default:
				break;
		}
				
		sb.append(s); // Construimos el campo
		//sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}




	public static String getFieldnameOperation() {
		return FIELDNAME_OPERATION;
	}




	public String getNicks() {
		return nicks;
	}




	public void setNicks(String nicks) {
		this.nicks = nicks;
	}
}
