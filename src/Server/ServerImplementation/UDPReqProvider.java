package Server.ServerImplementation;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

import Utility.Record;

public class UDPReqProvider extends Thread {
	private String recordCount = "";
	private String transferResult = "";
	private ServerImpl server;
	private String requestType;
	private Record recordForTransfer;
	Logger logger;


	public UDPReqProvider(ServerImpl server, String requestType,
						  Record recordForTransfer, Logger logger) throws IOException {
		this.server = server;
		this.requestType = requestType;
		this.recordForTransfer = recordForTransfer;
		this.logger=logger;
	}

	public String getRemoteRecordCount() {
		return recordCount;
	}

	public String getTransferResult() {
		return transferResult;
	}

	public synchronized void run() {
		DatagramSocket socket = null;
		try {
			System.out.println("Req type :: "+requestType);
			switch (requestType) {
			case "GET_RECORD_COUNT":
				socket = new DatagramSocket();
				byte[] data = "GET_RECORD_COUNT".getBytes();
				System.out.println("data in udp req provider: "+new String(data));
				System.out.println("port here :: "+server.locUDPPort);

				DatagramPacket packet = new DatagramPacket(data, data.length,
						InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()),
						server.locUDPPort);
				socket.send(packet);
				data = new byte[100];
				socket.receive(new DatagramPacket(data, data.length));
				recordCount = server.location + " " + new String(data);
				break;
			case "TRANSFER_RECORD":
				socket = new DatagramSocket();
				byte[] data1 = ("TRANSFER_RECORD" + "#"
						+ recordForTransfer.toString()).getBytes();
				

				DatagramPacket packet1 = new DatagramPacket(data1, data1.length,
						InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()),
						server.locUDPPort);
				socket.send(packet1);
				data1 = new byte[100];
				socket.receive(new DatagramPacket(data1, data1.length));
				transferResult = new String(data1);
				System.out.println("TRANSFER IN UDP PROVIDER: "+transferResult);
				break;
			}
		} catch (Exception e) {
			System.out.println("Exception: "+e.getMessage());
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

}