package Server.ServerImplementation;

import DCMSApp.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import Conf.Const;
import Utility.LogManager;
import Conf.Function;

import Utility.Record;

public class CreateReplicaRequest extends DCMSPOA {
	LogManager logManager;
	Logger logger;
	String IPaddress;
	public HashMap<String, List<Record>> recordsMap;
	int studentCount = 0;
	int teacherCount = 0;
	String recordsCount;
	String location;
	Integer requestId;
	HashMap<Integer, String> requestBuffer;
	Integer replicaID;

	public CreateReplicaRequest(Integer replicaID, Logger logger) {
		recordsMap = new HashMap<>();
		requestBuffer = new HashMap<>();
		requestId = 0;
		this.replicaID = replicaID;
		this.logger = logger;
	}


	private void sendMulticastRequest(String req) {
		MulticastSender sender = new MulticastSender(req, logger);
		sender.start();
	}

	@Override
	public String createTRecord(String managerID, String teacher) {
		teacher = Integer.toString(replicaID) + Const.RECEIVED_DATA_SEPERATOR + Function.CREATE_T_RECORD
				+ Const.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID) + Const.RECEIVED_DATA_SEPERATOR
				+ managerID + Const.RECEIVED_DATA_SEPERATOR + teacher;
		logger.log(Level.INFO, "Preparing Multicast request for Create Teacher record: " + teacher);
		sendMulticastRequest(teacher);
		return "";
	}

	private String getServerLoc(String managerID) {
		return managerID.substring(0, 3);
	}


	@Override
	public String createSRecord(String managerID, String student) {
		student = Integer.toString(replicaID) + Const.RECEIVED_DATA_SEPERATOR + Function.CREATE_S_RECORD
				+ Const.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID) + Const.RECEIVED_DATA_SEPERATOR
				+ managerID + Const.RECEIVED_DATA_SEPERATOR + student;
		sendMulticastRequest(student);
		logger.log(Level.INFO, "Preparing Multicast request for Create Student record: " + student);
		return "";
	}

	@Override
	public String getRecordCount(String manager) {
		String data[] = manager.split(Const.RECEIVED_DATA_SEPERATOR);
		String req = Integer.toString(replicaID) + Const.RECEIVED_DATA_SEPERATOR + Function.GET_REC_COUNT
				+ Const.RECEIVED_DATA_SEPERATOR + getServerLoc(data[0]) + Const.RECEIVED_DATA_SEPERATOR
				+ manager;
		sendMulticastRequest(req);
		logger.log(Level.INFO, "Preparing Multicast request for get record Count: " + req);
		return "";
	}

	@Override
	public String editRecord(String managerID, String recordID, String fieldname, String newvalue) {
		String editData = Integer.toString(replicaID) + Const.RECEIVED_DATA_SEPERATOR + Function.EDIT_RECORD
				+ Const.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID) + Const.RECEIVED_DATA_SEPERATOR
				+ managerID + Const.RECEIVED_DATA_SEPERATOR + recordID + Const.RECEIVED_DATA_SEPERATOR
				+ fieldname + Const.RECEIVED_DATA_SEPERATOR + newvalue;
		sendMulticastRequest(editData);
		logger.log(Level.INFO, "Preparing Multicast request for editRecord: " + editData);
		return "";
	}

	public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		String req = Integer.toString(replicaID) + Const.RECEIVED_DATA_SEPERATOR + Function.TRANSFER_RECORD
				+ Const.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID) + Const.RECEIVED_DATA_SEPERATOR
				+ managerID + Const.RECEIVED_DATA_SEPERATOR + recordID + Const.RECEIVED_DATA_SEPERATOR
				+ remoteCenterServerName;
		sendMulticastRequest(req);
		logger.log(Level.INFO, "Preparing Multicast request for transferRecord: " + req);
		return "";
	}

	@Override
	public String killServer(String location) {
		return null;
	}
}