package Server.ServerImplementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Logger;

import Conf.Const;

public class MulticastSender extends Thread {
	MulticastSocket multicastsocket;
	InetAddress address;
	String data;
	Logger logger;

	public MulticastSender(String request, Logger logger) {
		try {
			multicastsocket = new MulticastSocket(Const.MULTICAST_PORT_NUMBER);
			address = InetAddress.getByName(Const.MULTICAST_IP_ADDRESS);
			multicastsocket.joinGroup(address);
			this.logger = logger;
			this.data = request;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public synchronized void run() {
		try {
			DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), address,
					Const.MULTICAST_PORT_NUMBER);
			multicastsocket.send(packet);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
