package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
	    socket.close();
	    return downloaded;
	}



	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
