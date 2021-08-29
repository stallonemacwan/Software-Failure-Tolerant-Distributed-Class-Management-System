package Server.ServerImplementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import Conf.Const;
import Utility.LogManager;

import java.util.Arrays;

import Conf.Function;
import FrontEnd.FrontEndImpl;

public class ReplicaReqProcessor extends Thread {

	String currentOperationData;
	ServerImpl server;
	String response;
	LogManager logManager;

	public ReplicaReqProcessor(String operationData, LogManager logManager) {
		this.currentOperationData = operationData;
		this.server = null;
		response = null;
		this.logManager = logManager;
	}

	public synchronized void run() {
		String[] dataArr;
		String[] dataToBeSent = this.currentOperationData.trim().split(Const.RECEIVED_DATA_SEPERATOR);

		Integer replicaId = Integer.parseInt(dataToBeSent[0]);
		System.out.println("Currently serving replica with ID: " + replicaId);
		 Function oprn = Function.valueOf(dataToBeSent[1]);

		String requestId = dataToBeSent[dataToBeSent.length - 1];
		System.out.println("Currently serving request with id: " + requestId);

		switch (oprn) {
		case CREATE_T_RECORD:
			this.server = chooseServer(replicaId, dataToBeSent[2]);
			dataArr = Arrays.copyOfRange(dataToBeSent, 4, dataToBeSent.length);
			String teacherData = String.join(Const.RECEIVED_DATA_SEPERATOR, dataArr);
			response = this.server.createTRecord(dataToBeSent[3], teacherData);
			sendReply(response);
			break;
		case CREATE_S_RECORD:
			this.server = chooseServer(replicaId, dataToBeSent[2]);
			dataArr = Arrays.copyOfRange(dataToBeSent, 4, dataToBeSent.length);
			String studentData = String.join(Const.RECEIVED_DATA_SEPERATOR, dataArr);
			response = this.server.createSRecord(dataToBeSent[3], studentData);
			sendReply(response);
			break;
		case GET_REC_COUNT:
			this.server = chooseServer(replicaId, dataToBeSent[2]);
			response = this.server
					.getRecordCount(dataToBeSent[3] + Const.RECEIVED_DATA_SEPERATOR + dataToBeSent[4]);
			sendReply(response);
			break;
		case EDIT_RECORD:
			this.server = chooseServer(replicaId, dataToBeSent[2]);
			String newdata = dataToBeSent[6] + Const.RECEIVED_DATA_SEPERATOR + dataToBeSent[7];
			response = this.server.editRecord(dataToBeSent[3], dataToBeSent[4], dataToBeSent[5], newdata);
			sendReply(response);
			break;
		case TRANSFER_RECORD:
			this.server = chooseServer(replicaId, dataToBeSent[2]);
			String newdata1 = dataToBeSent[5] + Const.RECEIVED_DATA_SEPERATOR + dataToBeSent[6];
			response = this.server.transferRecord(dataToBeSent[3], dataToBeSent[4], newdata1);
			sendReply(response);
			break;
		}
	}

	public synchronized String getResponse() {
		return response;
	}

	private synchronized ServerImpl chooseServer(int replicaId, String loc) {
		return FrontEndImpl.centralRepository.get(replicaId).get(loc);
	}

	private synchronized void sendReply(String response) {
		DatagramSocket ds;
		try {
			ds = new DatagramSocket();
			byte[] dataBytes = response.getBytes();
			DatagramPacket dp = new DatagramPacket(dataBytes, dataBytes.length,
					InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()),
					Const.CURRENT_PRIMARY_PORT_FOR_REPLICAS);
			ds.send(dp);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
