package DCMSApp;


/**
* DCMSApp/DCMSOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from DCMS.idl
* Thursday, August 5, 2021 5:21:05 o'clock PM IST
*/

public interface DCMSOperations 
{
  String createTRecord (String managerID, String teacher);
  String createSRecord (String managerID, String student);
  String getRecordCount (String managerID);
  String editRecord (String managerID, String recordID, String fieldName, String newValue);
  String transferRecord (String managerID, String recordID, String location);
  String killServer (String location);
} // interface DCMSOperations
