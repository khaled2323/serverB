import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class RSS implements java.io.Serializable{
	private String name;
	private int socket;
	private int orderNum;
	private InetAddress IpAddress;
	private String request;
	private String status;
	private String reason;
	private String clientIp;
	private int serverSocket;
	private String subject;
	private String message;
	private ArrayList<String> subjects =null;
	private String sentFrom;

	public RSS(String n, int s, int nu, InetAddress ip, String r){
		name = n;
		socket = s;
		orderNum = nu;
		IpAddress = ip;
		request = r;
	}

	public RSS(int ss) {
		serverSocket = ss;
	}

	String gettClienName(){
		return name;
	}
	void setClientStatus(String m) {
		status = m;
	}
	String getClientStatus() {
		return status;
	}
	void setSubject(String m) {
		subject = m;
	}
	String getsubject() {
		return subject;
	}
	void setMessage(String m) {
		message = m;
	}
	String getMessage() {
		return message;
	}
	void setSubjects(ArrayList<String> m) {
		subjects = m;
	}
	ArrayList<String> getSubjects() {
		return subjects;
	}
	void setClientSimulationIp(String m) {
		clientIp = m;
	}
	String getClientSimulationIp() {
		return clientIp;
	}
	void setReason(String m) {
		reason = m;
	}
	String getReason() {
		return reason;
	}
	String getRequest(){
		return request;
	}
	int gettClientSocket(){
		return socket;
	}

	void setServerSocket(int ss) {
		serverSocket = ss;
	}

	int getServerSocket() {
		return serverSocket;
	}

	int getOrderNumber(){
		return orderNum;
	}

	void setFrom(String from) {
		this.sentFrom = from;
	}

	String from() {
		return sentFrom;
	}

	ArrayList<String> deleteSubjects(ArrayList<String> m) {
		m.clear();
		return m;

	}
	InetAddress gettClientIp(){
		return IpAddress;
	}

	void checkData() {
		System.out.println( " Client name is " + name + " Socket number " + socket + " Order Num" + orderNum +" IP " + IpAddress);
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{    out.defaultWriteObject();  }
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException
	{    in.defaultReadObject();  }

}
