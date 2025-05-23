package es.um.redes.nanoFiles.logic;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;





public class NFControllerLogicP2P {
	
	NFServer servidor;


	protected NFControllerLogicP2P() {
        try {
            servidor = new NFServer();
        } catch (IOException e) {
            System.out.println("Error al crear el servidor: " + e.getMessage());
        }
    }
	

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * @param controllerDir 
	 * 
	 */
	
	protected void foregroundServeFiles(NFControllerLogicDir controllerDir) {
		

		try {
            NFServerSimple servidorSimple = new NFServerSimple();
            controllerDir.registerFileServer(servidorSimple.getPuertodinamico());
            servidorSimple.run(); 
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor en primer plano: " + e.getMessage());
        }

	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean backgroundServeFiles() {
		
		return false;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param fserverAddr    La dirección del servidor al que se conectará
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 */
	protected boolean downloadFileFromSingleServer(InetSocketAddress fserverAddr, String targetFileHash,
			String localFileName) {
		boolean result = false;
		if (fserverAddr == null) {
			System.err.println("* Cannot start download - No server address provided");
			return false;
		}

		File localFile = new File(localFileName);
	    if (localFile.exists()) {
	        System.out.println("El archivo ya existe en esta máquina, no se realizará la descarga.");
	        return false;
	    }

	    try {
	        NFConnector nfConnector = new NFConnector(fserverAddr);
	        result = nfConnector.downloadFile(targetFileHash, localFile);
	        if (result) {
	            System.out.println("Se ha completado la descarga del archivo.");
	        }
	    } catch (IOException e) {
	        System.err.println("Se produjo un error de entrada/salida: " + e.getMessage());
	    }

	    return result;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList La lista de direcciones de los servidores a los que
	 *                          se conectará
	 * @param targetFileHash    Hash completo del fichero a descargar
	 * @param localFileName     Nombre con el que se guardará el fichero descargado
	 */
	public boolean downloadFileFromMultipleServers(LinkedList<InetSocketAddress> serverAddressList,
			String targetFileHash, String localFileName) {
		boolean downloaded = false;

		if (serverAddressList == null) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}

		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros en
	 * segundo plano
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	public int getServerPort() {
		int port = 0;


		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		



	}

}
