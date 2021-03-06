package Utility;

import java.io.Serializable;

public abstract class Record implements Serializable {
	private String firstName;
	private String lastname;
	private String recordID;

	public Record(String recordID, String firstName, String lastname) {
		this.setFirstName(firstName);
		this.setLastName(lastname);
		this.setRecordID(recordID);
	}

	public String getRecordID() {
		return recordID;
	}

	public void setRecordID(String recordID) {
		this.recordID = recordID;
	}

	public Record() {

	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastname;
	}

	public void setLastName(String lastname) {
		this.lastname = lastname;
	}

	public byte[] getBytes() {
		return this.getBytes();
	}
}