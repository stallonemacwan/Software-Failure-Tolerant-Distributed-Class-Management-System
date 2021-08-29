package Server.ServerImplementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;
import Conf.Const;
import Utility.LogManager;
import Conf.Location;
import FrontEnd.TransferRespToFrontEnd;

public class ReplicaRespReceiver extends Thread {

	DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	int udpPortNum;
	Location location;
	LogManager loggerInstance;
	String recordCount;
	HashMap<Integer, TransferRespToFrontEnd> responses;
	int c;

	public ReplicaRespReceiver(LogManager logManager) {
		try {
			loggerInstance = logManager;
			serverSocket = new DatagramSocket(Const.CURRENT_PRIMARY_PORT_FOR_REPLICAS);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public synchronized void run() {
		byte[] receiveData;
		while (true) {
			try {
				receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				byte[] receivedData = receivePacket.getData();
				String inputPacket = new String(receivedData).trim();
				if (inputPacket.contains("ACKNOWLEDGEMENT")) {
					System.out.println(new String(receivedData));
					loggerInstance.logger.log(Level.INFO, inputPacket);
				} else {
					System.out.println("Received response packet in PRIMARY: " + new String(receivedData));
					loggerInstance.logger.log(Level.INFO, "Received response in Primary " + inputPacket);
				}
			} catch (Exception e) {

			}
		}
	}
}
