import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
	this.serverSocket = ss;
}

int getServerSocket() {
	return serverSocket;
}

int getOrderNumber(){
	return orderNum;
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
