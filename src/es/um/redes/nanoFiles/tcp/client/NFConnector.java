package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream dis;
	private DataOutputStream dos;




	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * TODO Se crea el socket a partir de la dirección del servidor (IP, puerto). La
		 * creación exitosa del socket significa que la conexión TCP ha sido
		 * establecida.
		 */
		/*
		 * TODO Se crean los DataInputStream/DataOutputStream a partir de los streams de
		 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
		 * datos del servidor.
		 */
		
		// Crear el socket utilizando la dirección del servidor (IP, puerto)
        socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
        
        // Crear los DataInputStream/DataOutputStream a partir de los flujos de entrada/salida del socket
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());



	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		/*
		 * TODO: Construir objetos PeerMessage que modelen mensajes con los valores
		 * adecuados en sus campos (atributos), según el protocolo diseñado, y enviarlos
		 * al servidor a través del "dos" del socket mediante el método
		 * writeMessageToOutputStream.
		 */
		/*
		 * TODO: Recibir mensajes del servidor a través del "dis" del socket usando
		 * PeerMessage.readMessageFromInputStream, y actuar en función del tipo de
		 * mensaje recibido, extrayendo los valores necesarios de los atributos del
		 * objeto (valores de los campos del mensaje).
		 */
		/*
		 * TODO: Para escribir datos de un fichero recibidos en un mensaje, se puede
		 * crear un FileOutputStream a partir del parámetro "file" para escribir cada
		 * fragmento recibido (array de bytes) en el fichero mediante el método "write".
		 * Cerrar el FileOutputStream una vez se han escrito todos los fragmentos.
		 */
		/*
		 * NOTA: Hay que tener en cuenta que puede que la subcadena del hash pasada como
		 * parámetro no identifique unívocamente ningún fichero disponible en el
		 * servidor (porque no concuerde o porque haya más de un fichero coincidente con
		 * dicha subcadena)
		 */

		/*
		 * TODO: Finalmente, comprobar la integridad del fichero creado para comprobar
		 * que es idéntico al original, calculando el hash a partir de su contenido con
		 * FileDigest.computeFileChecksumString y comparándolo con el hash completo del
		 * fichero solicitado. Para ello, es necesario obtener del servidor el hash
		 * completo del fichero descargado, ya que quizás únicamente obtuvimos una
		 * subcadena del mismo como parámetro.
		 */
		
		boolean downloaded = false;
		String hash = null;

	    FileInfo fileInfo = new FileInfo();
	    fileInfo.fileHash = targetFileHashSubstr;
	    fileInfo.fileName = file.getName();
	    PeerMessage message = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD, fileInfo);
	    message.writeMessageToOutputStream(dos);
	    PeerMessage response = PeerMessage.readMessageFromInputStream(dis);
	    if (response.getOpcode() == PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA) {
	        // Create a FileOutputStream to write the file data
	    	FileOutputStream fichero = new FileOutputStream(file);
	    	fichero.write(response.getData());
	    	while(downloaded == false) {
	    		PeerMessage datos = PeerMessage.readMessageFromInputStream(dis);
				if (PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA == datos.getOpcode()) {
					fichero.write(datos.getData());
				}
				else if (PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_OK == datos.getOpcode()){
					downloaded = true;
					hash = new String(datos.getFileInfo().fileHash.getBytes(),StandardCharsets.UTF_8);
				}
			}
	    	fichero.close();
			downloaded = FileDigest.computeFileChecksumString(file.getName()).equals(hash);
			if (!downloaded) System.err.println("El hash no coincide");
	    		
	   
	    	
	    } else if (response.getOpcode() == PeerMessageOps.OPCODE_DOWNLOAD_FAIL) {
	    	System.err.println("Error al descargar el archivo. Puede ser que no exista el fichero o que hayan varios con el mismo hash.");
	    }

	    return downloaded;
	}



	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
