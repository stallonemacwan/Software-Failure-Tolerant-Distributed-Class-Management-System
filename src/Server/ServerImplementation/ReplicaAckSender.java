package Server.ServerImplementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import Conf.Const;
import Utility.LogManager;

public class ReplicaAckSender extends Thread {
	String request;
	DatagramSocket ds;

	public ReplicaAckSender(String request, LogManager logManger) {
		request = "RECEIVED ACKNOWLEDGEMENT IN PRIMARY: " + request;
		this.request = request;
	}

	public synchronized void run() {
		try {
			ds = new DatagramSocket();
			byte[] dataBytes = request.getBytes();
			DatagramPacket dp = new DatagramPacket(dataBytes, dataBytes.length,
					InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()),
					Const.CURRENT_PRIMARY_PORT_FOR_REPLICAS);
			ds.send(dp);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
