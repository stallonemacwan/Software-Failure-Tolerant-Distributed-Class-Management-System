package Server.ServerImplementation;

import DCMSApp.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import Conf.Const;
import FrontEnd.FrontEndImpl;
import Server.HeartBeat.HeartBeatReceiver;
import Server.HeartBeat.HeartBeatSender;
import Utility.LogManager;
import Conf.Location;
import Utility.Record;
import Utility.Student;
import Utility.Teacher;

/**
 * 
 * DCMS ServerImpl class includes all the server operations' implementations,
 * implements all the methods in the IDL interface Performs the necessary
 * operations and returns the result/acknowledgement back to the Client.
 *
 */

public class ServerImpl extends DCMSPOA {
	private LogManager logManager;
	public HashMap<String, List<Record>> recordsMap;
	public HeartBeatReceiver heartBeatReceiver;
	public ArrayList<Integer> replicas;
	UDPReceiver DCMSServerUDPReceiver;
	String IPaddress;
	Object recordsMapAccessorLock = new Object();
	int studentCount = 0;
	int teacherCount = 0;
	String recordsCount;
	String location;
	int locUDPPort = 0;
	boolean isPrimary;
	Integer serverID = 0;
	HeartBeatSender heartBeatSender;
	String name;
	int port1, port2;
	boolean isAlive;
	DatagramSocket ds = null;

	public int getlocUDPPort() {
		return this.locUDPPort;
	}

	public ServerImpl(int serverID, boolean isPrimary, Location loc, int locUDPPort, DatagramSocket ds,
					  boolean isAlive, String name, int receivePort, int port1, int port2, ArrayList<Integer> replicas,
					  LogManager logger) {
		logManager = logger;
		synchronized (recordsMapAccessorLock) {
			recordsMap = new HashMap<>();
		}
		this.locUDPPort = locUDPPort;
		DCMSServerUDPReceiver = new UDPReceiver(true, locUDPPort, loc, logManager.logger, this);
		DCMSServerUDPReceiver.start();
		location = loc.toString();
		this.isPrimary = isPrimary;
		this.serverID = serverID;
		this.name = name;
		this.port1 = port1;
		this.port2 = port2;
		this.isAlive = isAlive;
		heartBeatReceiver = new HeartBeatReceiver(isAlive, name, receivePort, logManager.logger);
		heartBeatReceiver.start();
		this.ds = ds;
		this.replicas = replicas;
	}

	@Override
	public synchronized String createTRecord(String managerID, String teacher) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				CreateReplicaRequest req = new CreateReplicaRequest(replicaId,
						logManager.logger);
				req.createTRecord(managerID, teacher);
			}
		}
		String temp[] = teacher.split(",");
		String teacherID = "TR" + (++teacherCount);
		String firstName = temp[0];
		String lastname = temp[1];
		String address = temp[2];
		String phone = temp[3];
		String specialization = temp[4];
		String location = temp[5];
		String requestID = temp[6];
		Teacher teacherObj = new Teacher(managerID, teacherID, firstName, lastname, address, phone, specialization,
				location);
		String key = lastname.substring(0, 1);
		String message = addRecordToHashMap(key, teacherObj, null);
		if (message.equals("success")) {
			System.out.println("teacher is added " + teacherObj + " with this key " + key + " by Manager " + managerID
					+ " for the request ID: " + requestID);
			logManager.logger.log(Level.INFO, "Teacher record created " + teacherID + " by Manager : " + managerID
					+ " for the request ID: " + requestID);
		} else {
			logManager.logger.log(Level.INFO, "Error in creating T record" + requestID);
			return "Error in creating T record";
		}

		return teacherID;

	}

	@Override
	public synchronized String createSRecord(String managerID, String student) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				CreateReplicaRequest req = new CreateReplicaRequest(replicaId,
						logManager.logger);
				req.createSRecord(managerID, student);
			}
		}
		String temp[] = student.split(",");
		String firstName = temp[0];
		String lastName = temp[1];
		String CoursesRegistered = temp[2];
		List<String> courseList = putCoursesinList(CoursesRegistered);
		String status = temp[3];
		String statusDate = temp[4];
		String requestID = temp[5];
		String studentID = "SR" + (++studentCount);
		Student studentObj = new Student(managerID, studentID, firstName, lastName, courseList, status, statusDate);
		String key = lastName.substring(0, 1);
		String message = addRecordToHashMap(key, null, studentObj);
		if (message.equals("success")) {
			System.out.println(" Student is added " + studentObj + " with this key " + key + " by Manager " + managerID
					+ " for the requestID " + requestID);
			logManager.logger.log(Level.INFO, "Student record created " + studentID + " by manager : " + managerID
					+ " for the requestID " + requestID);
		} else {
			return "Error in creating S record";
		}
		return studentID;
	}

	private synchronized int getCurrServerCnt() {
		int count = 0;
		synchronized (recordsMapAccessorLock) {
			for (Map.Entry<String, List<Record>> entry : this.recordsMap.entrySet()) {
				List<Record> list = entry.getValue();
				count += list.size();
			}
		}
		return count;
	}

	@Override
	public synchronized String getRecordCount(String manager) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				CreateReplicaRequest req = new CreateReplicaRequest(replicaId,
						logManager.logger);
				req.getRecordCount(manager);
			}
		}
		String data[] = manager.split(Const.RECEIVED_DATA_SEPERATOR);
		String managerID = data[0];
		String requestID = data[1];
		String recordCount = null;
		UDPReqProvider[] req = new UDPReqProvider[2];
		int counter = 0;
		ArrayList<String> locList = new ArrayList<>();
		locList.add("MTL");
		locList.add("LVL");
		locList.add("DDO");
		for (String loc : locList) {
			if (loc == this.location) {
				recordCount = loc + " " + getCurrServerCnt();
			} else {
				try {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("Server id -> " + serverID);
					req[counter] = new UDPReqProvider(
							FrontEndImpl.centralRepository.get(serverID).get(loc), "GET_RECORD_COUNT", null,
							logManager.logger);
				} catch (IOException e) {
					System.out.println("Exception in get rec count -> " + e.getMessage());
					logManager.logger.log(Level.SEVERE, e.getMessage());
				}
				req[counter].start();
				counter++;
			}
		}
		for (UDPReqProvider request : req) {
			try {
				request.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			recordCount += " , " + request.getRemoteRecordCount().trim();
		}
		System.out.println(
				recordCount + " for the request ID " + requestID + " as requested by the managerID " + managerID);
		logManager.logger.log(Level.INFO,
				recordCount + " for the request ID " + requestID + " as requested by the managerID " + managerID);
		return recordCount;
	}


	@Override
	public synchronized String editRecord(String managerID, String recordID, String fieldname, String newvalue) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				CreateReplicaRequest req = new CreateReplicaRequest(replicaId,
						logManager.logger);
				req.editRecord(managerID, recordID, fieldname, newvalue);
			}
		}
		String data[] = newvalue.split(Const.RECEIVED_DATA_SEPERATOR);
		String requestID = data[1];
		String type = recordID.substring(0, 2);
		if (type.equals("TR")) {
			return editTRRecord(managerID, recordID, fieldname, newvalue);
		} else if (type.equals("SR")) {
			return editSRRecord(managerID, recordID, fieldname, newvalue);
		}
		logManager.logger.log(Level.INFO, "Record edit successful for the request ID " + requestID);
		return "Operation not performed!";
	}

	public synchronized String transferRecord(String managerID, String recordID, String data) {

		if (isPrimary) {
			for (Integer replicaId : replicas) {
				CreateReplicaRequest req = new CreateReplicaRequest(replicaId,
						logManager.logger);
				req.transferRecord(managerID, recordID, data);
			}
		}
		String parsedata[] = data.split(Const.RECEIVED_DATA_SEPERATOR);
		String remoteCenterServerName = parsedata[0];
		String requestID = parsedata[1];
		String type = recordID.substring(0, 2);
		UDPReqProvider req = null;
		UDPReqProvider req1 = null;
		try {
			Record record = getRecordForTransfer(recordID);
			if (record == null) {
				return "RecordID unavailable!";
			} else if (remoteCenterServerName.equals(this.location)) {
				return "Please enter a valid location to transfer. The record is already present in " + location;
			}
			req = new UDPReqProvider(
					FrontEndImpl.centralRepository.get(serverID).get(remoteCenterServerName.trim()), "TRANSFER_RECORD",
					record, logManager.logger);

			if (isPrimary && this.replicas.size() == Const.TOTAL_REPLICAS_COUNT - 1) {
				System.out.println("Replicas size is ->->->->->: 1" + remoteCenterServerName);
				req1 = new UDPReqProvider(FrontEndImpl.centralRepository.get(Const.REPLICA2_SERVER_ID)
						.get(remoteCenterServerName.trim()), "TRANSFER_RECORD", record, logManager.logger);
				req1.start();
				try {
					req1.join();
					backupAfterTransferRecord(Const.REPLICA2_SERVER_ID, remoteCenterServerName);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			logManager.logger.log(Level.SEVERE, e.getMessage());
		}
		if (req != null) {
			req.start();
		}
		try {
			if (req != null) {
				req.join();
			}
			if (removeRecordAfterTransfer(recordID) == "success") {
				logManager.logger.log(Level.INFO, "Record created in  " + remoteCenterServerName + "  and removed from "
						+ location + " with requestID " + requestID);
				System.out.println("Record created in " + remoteCenterServerName + "and removed from " + location
						+ " with requestID " + requestID);
				takeTheBackup();
				backupAfterTransferRecord(this.serverID, remoteCenterServerName);
				return "Record transferred from " + location + " to " + remoteCenterServerName;
			}
		} catch (Exception e) {
			System.out.println("Exception in transfer record -> " + e.getMessage());
		}

		return "Transfer record operation unsuccessful!";
	}

	private synchronized String removeRecordAfterTransfer(String recordID) {
		synchronized (recordsMapAccessorLock) {
			for (Entry<String, List<Record>> element : recordsMap.entrySet()) {
				List<Record> mylist = element.getValue();
				for (int i = 0; i < mylist.size(); i++) {
					if (mylist.get(i).getRecordID().equals(recordID)) {
						mylist.remove(i);
					}
				}
				recordsMap.put(element.getKey(), mylist);
			}
			System.out.println("Removed record from " + this.location);
		}
		return "success";
	}

	private synchronized Record getRecordForTransfer(String recordID) {
		synchronized (recordsMapAccessorLock) {
			for (Entry<String, List<Record>> value : recordsMap.entrySet()) {
				List<Record> mylist = value.getValue();
				Optional<Record> record = mylist.stream().filter(x -> x.getRecordID().equals(recordID)).findFirst();
				if (recordID.contains("TR")) {
					if (record.isPresent())
						return (Teacher) record.get();
				} else {
					if (record.isPresent())
						return (Student) record.get();
				}
			}
		}
		return null;
	}

	private synchronized String editSRRecord(String managerID, String recordID, String fieldname, String data) {
		String newdata[] = data.split(Const.RECEIVED_DATA_SEPERATOR);
		String newvalue = newdata[0];
		String requestID = newdata[1];
		for (Entry<String, List<Record>> value : recordsMap.entrySet()) {
			List<Record> mylist = value.getValue();
			Optional<Record> record = mylist.stream().filter(x -> x.getRecordID().equals(recordID)).findFirst();
			if (record.isPresent()) {
				if (record.isPresent() && fieldname.equals("Status")) {
					((Student) record.get()).setStatus(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + " and Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with status -> " + newvalue;
				} else if (record.isPresent() && fieldname.equals("StatusDate")) {
					((Student) record.get()).setStatusDate(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with status date -> " + newvalue;
				} else if (record.isPresent() && fieldname.equals("CoursesRegistered")) {
					List<String> courseList = putCoursesinList(newvalue);
					((Student) record.get()).setCoursesRegistered(courseList);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the courses registered\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with courses -> " + courseList;
				} else {
					System.out.println("Record with " + recordID + " not found");
					logManager.logger.log(Level.INFO, "Record with " + recordID + "not found!" + location);
					return "Record with " + recordID + " not found";
				}
			}
		}
		return "Record with " + recordID + "not found!";
	}

	private synchronized String editTRRecord(String managerID, String recordID, String fieldname, String data) {
		String newdata[] = data.split(Const.RECEIVED_DATA_SEPERATOR);
		String newvalue = newdata[0];
		String requestID = newdata[1];
		for (Entry<String, List<Record>> val : recordsMap.entrySet()) {
			List<Record> mylist = val.getValue();
			Optional<Record> record = mylist.stream().filter(x -> x.getRecordID().equals(recordID)).findFirst();

			if (record.isPresent()) {
				if (record.isPresent() && fieldname.equals("Phone")) {
					((Teacher) record.get()).setPhone(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with Phone -> " + newvalue;
				}

				else if (record.isPresent() && fieldname.equals("Address")) {
					((Teacher) record.get()).setAddress(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with address -> " + newvalue;
				}

				else if (record.isPresent() && fieldname.equals("Location")) {
					((Teacher) record.get()).setLocation(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with location -> " + newvalue;
				} else {
					System.out.println("Record with " + recordID + " not found");
					logManager.logger.log(Level.INFO, "Record with " + recordID + "not found!" + location);
					return "Record with " + recordID + " not found";
				}
			}
		}
		return "Record with " + recordID + " not found";
	}

	public void send() {
		heartBeatSender = new HeartBeatSender(ds, name, port1, port2);
		heartBeatSender.start();
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public ArrayList<Integer> getReplicas() {
		return replicas;
	}

	public void setReplicas(ArrayList<Integer> replicas) {
		this.replicas = replicas;
	}

	@Override
	public String killServer(String location) {
		return null;
	}

	public Integer getServerID() {
		return serverID;
	}

	public void setServerID(Integer serverID) {
		this.serverID = serverID;
	}

	public synchronized void takeTheBackup() {
		synchronized (recordsMapAccessorLock) {
			if (this.location.equalsIgnoreCase("MTL") && serverID == 1 && recordsMap.size() > 0) {
				FrontEndImpl.S1_MTL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("LVL") && serverID == 1 && recordsMap.size() > 0) {
				FrontEndImpl.S1_LVL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("DDO") && serverID == 1 && recordsMap.size() > 0) {
				FrontEndImpl.S1_DDO.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("MTL") && serverID == 2 && recordsMap.size() > 0) {
				FrontEndImpl.S2_MTL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("LVL") && serverID == 2 && recordsMap.size() > 0) {
				FrontEndImpl.S2_LVL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("DDO") && serverID == 2 && recordsMap.size() > 0) {
				FrontEndImpl.S2_DDO.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("MTL") && serverID == 3 && recordsMap.size() > 0) {
				FrontEndImpl.S3_MTL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("LVL") && serverID == 3 && recordsMap.size() > 0) {
				FrontEndImpl.S3_LVL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("DDO") && serverID == 3 && recordsMap.size() > 0) {
				FrontEndImpl.S3_DDO.backupMap(this.recordsMap);
			}
		}
	}

	public void backupAfterTransferRecord(Integer serverID, String remoteCenterServerName) {
		synchronized (recordsMapAccessorLock) {
			HashMap<String, ServerImpl> serverList = FrontEndImpl.centralRepository.get(serverID);
			ServerImpl remoteServer = serverList.get(remoteCenterServerName);
			if (remoteServer != null) {
				remoteServer.takeTheBackup();
			}
		}
	}

	public synchronized String addRecordToHashMap(String key, Teacher teacher, Student student) {
		String message = "Error";
		if (teacher != null) {
			List<Record> recordList = null;
			synchronized (recordsMapAccessorLock) {
				recordList = recordsMap.get(key);
			}
			if (recordList != null) {
				recordList.add(teacher);
			} else {
				List<Record> records = null;
				synchronized (recordsMapAccessorLock) {
					records = new ArrayList<Record>();
					records.add(teacher);
				}
				recordList = records;
			}
			synchronized (recordsMapAccessorLock) {
				recordsMap.put(key, recordList);
			}
			message = "success";
		}

		if (student != null) {
			List<Record> recordList = null;
			synchronized (recordsMapAccessorLock) {
				recordList = recordsMap.get(key);
			}
			if (recordList != null) {
				recordList.add(student);
			} else {
				List<Record> records = null;
				synchronized (recordsMapAccessorLock) {
					records = new ArrayList<Record>();
					records.add(student);
				}
				recordList = records;
			}
			synchronized (recordsMapAccessorLock) {
				recordsMap.put(key, recordList);
			}
			message = "success";
		}
		takeTheBackup();
		return message;
	}

	public synchronized List<String> putCoursesinList(String newvalue) {
		String[] courses = newvalue.split("//");
		ArrayList<String> courseList = new ArrayList<>();
		for (String course : courses)
			courseList.add(course);
		return courseList;
	}
}