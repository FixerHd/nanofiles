package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {


	private FileInfo fileInfo;
	private byte opcode;
	private byte[] data;


	
	
	public PeerMessage() {
		this.opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}


	public PeerMessage(byte opcode, FileInfo fileInfo) {
		this.opcode = opcode;
		this.fileInfo = fileInfo;
		this.data = null;
	}
	
	public PeerMessage(byte opcode, FileInfo fileInfo, byte[] data) {
		this.opcode = opcode;
		this.fileInfo = fileInfo;
		this.data = data;
	}


	


	public byte getOpcode() {
		return opcode;
	}
	
	public byte[] getData() {
		return data;
	}


	public void setData(byte[] data) {
		this.data = data;
	}

	public FileInfo getFileInfo() {
		if (fileInfo != null) {
			return fileInfo;
		} else {
			throw new IllegalStateException("FileInfo has not been set.");
		}
	}

	public void setFileInfo(FileInfo fileInfo) { 
		if (fileInfo != null) {
			this.fileInfo = fileInfo;
		} else {
			throw new IllegalArgumentException("FileInfo cannot be null.");
		}
	}




	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD:{
			int fileNameLength = dis.readInt();
		    byte[] fileNameBytes = new byte[fileNameLength];
		    fileNameBytes = dis.readNBytes(fileNameLength);
		    String fileName = new String(fileNameBytes);
		    
		    int fileHashLength = dis.readInt();
		    byte[] fileHashBytes = new byte[fileHashLength];
		    fileHashBytes = dis.readNBytes(fileHashLength);
		    String fileHash = new String(fileHashBytes);
		    
		    message = new PeerMessage(opcode, new FileInfo(fileHash, fileName, 0, null));
		    break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_FAIL:{
			message = new PeerMessage(opcode, new FileInfo());
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA:{
			int hashLength = dis.readInt();
			byte[] data = new byte[hashLength];
			dis.readFully(data);
			message = new PeerMessage(opcode, null, data);
			break;
		} case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_OK:{
			int length = dis.readInt(); // Lee la longitud del array
			byte[] fileHashBytes = new byte[length]; // Crea un array de bytes con la longitud leída
			dis.readFully(fileHashBytes); // Lee el array de bytes
			String fileHash = new String(fileHashBytes, StandardCharsets.UTF_8);
			message = new PeerMessage(opcode, new FileInfo(fileHash, "", 0, null));
			break;
		}


		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		
		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD:{
			if (fileInfo != null) {
		        byte[] fileNameBytes = fileInfo.fileName.getBytes();
		        dos.writeInt(fileNameBytes.length);
		        dos.write(fileNameBytes);
		        
		        byte[] fileHashBytes = fileInfo.fileHash.getBytes();
	            dos.writeInt(fileHashBytes.length);
	            dos.write(fileHashBytes);
		    }
		    break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_FAIL:{
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA:{
			assert(data.length > 0);
			dos.writeInt(data.length);
			dos.write(data);
			break;
		} case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_OK:{
			byte[] fileHashBytes = fileInfo.fileHash.getBytes(StandardCharsets.UTF_8);
			dos.writeInt(fileHashBytes.length); // Escribe la longitud del array
			dos.write(fileHashBytes); // Escribe el array de bytes
            break;
		}

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

}
