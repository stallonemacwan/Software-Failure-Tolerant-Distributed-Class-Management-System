package FrontEnd;
public class TransferRespToFrontEnd extends Thread {
	String response;
	public TransferRespToFrontEnd(String response) {
		this.response = response;
	}
	
	public void run() {
		System.out.println("Response - > "+this.response);
		FrontEndImpl.receivedResponses.add(this.response);
	}	

	public String getResponse() {
		return response;
	}
}
