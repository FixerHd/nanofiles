package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import es.um.redes.nanoFiles.util.FileInfo;
public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		
		PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA, new FileInfo(null, nombreArchivo, 0, null));
		msgOut.writeMessageToOutputStream(fos);
		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream(fis);
		/*
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}else if(!msgOut.getFileInfo().equals(msgIn.getFileInfo())) {
			System.out.print(msgOut.getFileInfo());
			System.out.println();
			System.out.print(msgIn.getFileInfo());
			System.out.println();
			
			//System.err.println("fileinfo does not match!");
		}
		else {
			System.out.println("exito.");
			System.out.print(msgOut.getFileInfo());
			System.out.println();
			System.out.print(msgIn.getFileInfo());
			System.out.println();
		}
	}

}

