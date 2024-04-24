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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

	            	FileInfo ficheros[] = NanoFiles.db.getFiles();
	            	FileInfo[] ficherosCoinciden =
							FileInfo.lookupHashSubstring(ficheros, new String(message.getFileInfo().fileHash.getBytes(),StandardCharsets.UTF_8));
	            	if (ficherosCoinciden.length == 0 || ficherosCoinciden.length > 1) {
						PeerMessage respuesta = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FAIL, null);
						respuesta.writeMessageToOutputStream(dos);
						break;

	            	}	else {
						
	                    String targetHash = ficherosCoinciden[0].fileHash;
	                    String filePath = NanoFiles.db.lookupFilePath(targetHash);
	
	                    File file = new File(filePath);
	                    FileInputStream fis = new FileInputStream(file);
	                    long filelength = file.length();
	    				byte data[] = new byte[(int) filelength];
	    				fis.read(data);
	    				fis.close();
	    				int numMsg = (data.length/32768)+1;									//Mensajes de 48000 bytes necesarios
	    				for(int i = 0; i<numMsg; i++) {										//Iterar sobre el contenido del fichero,
	    					byte[] aux;														//creando por cada 48000 bytes un mensaje
	    					if((i+1)*32768<=data.length)
	    						aux = Arrays.copyOfRange(data, i*32768, (i+1)*32768);		//Si necesitan varios mensajes
	    					else
	    						aux = Arrays.copyOfRange(data, i*32768, data.length);		//Si solo se necesita un mensaje
	    					PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA, null, aux);
	    					msg.writeMessageToOutputStream(dos);
	    				}
	    				PeerMessage dlOk = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_OK, new FileInfo(targetHash, "", 0, filePath));
	    				dlOk.writeMessageToOutputStream(dos);
	    				System.out.println("Fichero enviado correctamente");
	                    break;
	            	}
                }
                
                default: {
                    System.err.println("PeerMessage.readMessageFromInputStream no sabe como parsear este código: "
                            + PeerMessageOps.opcodeToOperation(opcode));
                    System.exit(-1);
                }
	            }
	            
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

		
	}




}
