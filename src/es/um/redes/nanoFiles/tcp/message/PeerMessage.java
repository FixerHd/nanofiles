package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {


	private FileInfo fileInfo;

	private byte opcode;


	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	
	public PeerMessage() {
		this.opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}


	public PeerMessage(byte opcode, FileInfo fileInfo) {
		this.opcode = opcode;
		this.fileInfo = fileInfo;
	}


	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() {
		return opcode;
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
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD:{
			int fileNameLength = dis.readInt();
		    byte[] fileNameBytes = new byte[fileNameLength];
		    fileNameBytes = dis.readNBytes(fileNameLength);
		    String fileName = new String(fileNameBytes);
		    message = new PeerMessage(opcode, new FileInfo(null, fileName, 0, null));
		    break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_FAIL:{
			message = new PeerMessage(opcode, null);
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA:{
			String fileHash = dis.readUTF();
			String fileName = dis.readUTF();
			long fileSize = dis.readLong();
			String filePath = dis.readUTF();
			message = new PeerMessage(opcode, new FileInfo(fileHash, fileName, fileSize, filePath));
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_FAIL:{
			message = new PeerMessage(opcode, null);
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
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD:{
			if (fileInfo != null) {
		        byte[] fileNameBytes = fileInfo.fileName.getBytes();
		        dos.writeInt(fileNameBytes.length);
		        dos.write(fileNameBytes);
		    }
		    break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_FAIL:{
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_DATA:{
			if (fileInfo != null) {
				dos.writeBytes(fileInfo.fileHash);
				dos.writeBytes(fileInfo.fileName);
				dos.writeLong(fileInfo.fileSize);
				dos.writeBytes(fileInfo.filePath);
			}
			break;
		}
		case PeerMessageOps.OPCODE_DOWNLOAD_RESPONSE_FAIL:{
			break;
		}

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

}
