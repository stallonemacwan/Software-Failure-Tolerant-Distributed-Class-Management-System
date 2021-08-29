package Server.ServerImplementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import Utility.LogManager;
import Conf.Const;

public class MulticastReceiver extends Thread {
	MulticastSocket multicastsocket;
	InetAddress address;
	boolean isPrimary;
	LogManager logManager;

	public MulticastReceiver(boolean isPrimary, LogManager ackManager) {
		try {
			multicastsocket = new MulticastSocket(Const.MULTICAST_PORT_NUMBER);
			address = InetAddress.getByName(Const.MULTICAST_IP_ADDRESS);
			multicastsocket.joinGroup(address);
			this.isPrimary = isPrimary;
			this.logManager = ackManager;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public synchronized void run() {
		try {
			while (true) {
				byte[] mydata = new byte[100];
				DatagramPacket packet = new DatagramPacket(mydata, mydata.length);
				multicastsocket.receive(packet);
				if (!isPrimary) {
					System.out.println("Received data in multicast heartBeatReceiver " + new String(packet.getData()));
					System.out.println("Sent the acknowledgement for the data received in replica to primary server "
							+ new String(packet.getData()));

					ReplicaAckSender ack = new ReplicaAckSender(
							new String(packet.getData()), logManager);
					ack.start();
					ReplicaReqProcessor req = new ReplicaReqProcessor(
							new String(packet.getData()), logManager);
					req.start();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
