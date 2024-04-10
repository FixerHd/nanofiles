package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: Crear dis/dos a partir del socket
		 */
		/*
		 * TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
		 */
		/*
		 * TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
		 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
		 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
		 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
		 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
		 * devuelve la ruta al fichero a partir de su hash completo.
		 */
		try {
	        // Crear dis/dos a partir del socket
	        DataInputStream dis = new DataInputStream(socket.getInputStream());
	        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

	        while (!socket.isClosed()) {
	            PeerMessage message = PeerMessage.readMessageFromInputStream(dis);
	            byte opcode = message.getOpcode();

	            switch (opcode) {
	            case PeerMessageOps.OPCODE_DOWNLOAD: {

                    FileInfo fileInfo = message.getFileInfo();
                    String targetHash = fileInfo.fileHash;
                    String filePath = NanoFiles.db.lookupFilePath(targetHash);

                    File file = new File(filePath);
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                    dos.flush();
                    break;
                }
                case PeerMessageOps.OPCODE_DOWNLOAD_FAIL: {
                    dos.writeUTF("La descarga ha fallado.");
                    break;
                }
                case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA: {
                    
                    FileInfo fileInfo = message.getFileInfo();
                    String filePath = fileInfo.filePath;
                    File file = new File(filePath);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = dis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    break;
                }
                default: {
                    System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
                            + PeerMessageOps.opcodeToOperation(opcode));
                    System.exit(-1);
                }
	            }

	            // Para servir un fichero, hay que localizarlo a partir de su hash (o
	            // subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
	            // compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
	            // FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
	            // subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
	            // devuelve la ruta al fichero a partir de su hash completo.
	            FileInfo fileInfo = message.getFileInfo();
	            String targetHash = fileInfo.fileHash;
	            String filePath = NanoFiles.db.lookupFilePath(targetHash);
	            File archivo = new File(filePath);
	            FileInputStream fis = new FileInputStream(archivo);
	            byte[] buffer = new byte[4096];
	            int bytesRead = -1;
	            while ((bytesRead = fis.read(buffer)) != -1) {
	                dos.write(buffer, 0, bytesRead);
	            }
	            fis.close();
	            dos.flush();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }


	}




}
