import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler implements Runnable {
	private DatagramSocket ds;
	private BufferedReader in;
	private PrintWriter out;
	Object inputObject;
	static HashMap<String, ArrayList<String>> storage = new HashMap<String, ArrayList<String>>();

	String requestType;
	RSS clientR;
	String input;

	byte[] receive = new byte[65535];
	DatagramPacket DpReceive = null;
	InetAddress ip = InetAddress.getLocalHost();
	//InetAddress ip = InetAddress.getByName("8.8.8.8");

	public ClientHandler(DatagramSocket clientSocket) throws IOException {
        this.ds = clientSocket;
		//clientR.getRequest();
    } // end of ClientHandler

	@Override
    public void run() {
		try {
			while (true) {
				DpReceive = new DatagramPacket(receive, receive.length);

				ds.receive(DpReceive);

				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receive));

				try {
					inputObject = iStream.readObject();
					if (inputObject instanceof HashMap) {
						System.out.println("Storage from Server A:\n" + inputObject); // used for testing
						storage = (HashMap) inputObject;
						break;
					}
					else if (inputObject instanceof RSS)
						clientR = (RSS) iStream.readObject();

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// print which client the server thread is listening to
				System.out.println("\nServer Listening to Client: " + clientR.getClientSimulationIp() + ":" + clientR.gettClientSocket());

				iStream.close();
				System.out.println("Client Name: " + clientR.gettClienName());
				System.out.println("Client Request: " + clientR.getRequest());
				System.out.println("Order #: " + clientR.getOrderNumber());
				System.out.println("Client IP: " + clientR.getClientSimulationIp());
				System.out.println("Client Socket Number: " + clientR.gettClientSocket());
				String Key;
				String data;
				Key = clientR.gettClienName();
				data = clientR.getRequest() + " " + clientR.getOrderNumber() + " " + clientR.getClientSimulationIp() + " " + clientR.gettClientSocket();
				ArrayList<String> b = new ArrayList<String>();
				if (clientR.getRequest().equals("REGISTER")) {
					// TODO create condition to check registration success
					if (storage.containsKey(Key) == false) {

						System.out.println("A Register request is made by " + clientR.gettClienName());
						// save data
						b.add(data);
						storage.put(Key, b); // add to hashmap
						b = null; // to be used to point to other places in hashmap arrayList
						// send back confirmation
						System.out.println("Registration accepted");
						clientR.setClientStatus("REGISTERED");
						System.out.println(" ");
						System.out.println("*****************************************************");
						System.out.println("   STORAGE CONTENT   ");
						System.out.println(storage);
						ByteArrayOutputStream bStream = new ByteArrayOutputStream();
						sendUDP(bStream, clientR, ds, ip);
					} else if (storage.containsKey(Key) == true) {
						//System.out.println( " status currently " + clientR.getClientStatus());
						clientR.setClientStatus("REGISTER-DENIED");
						clientR.setReason("User Already Registered");
						System.out.println("REGISTER-DENIED");
						ByteArrayOutputStream bStream = new ByteArrayOutputStream();
						sendUDP(bStream, clientR, ds, ip);
					}
				} else if (clientR.getRequest().equals("DE-REGISTER")) {
					if (storage.containsKey(Key) == true) { //if user data exists
						storage.remove(Key); //delete from hashmap
						clientR.setClientStatus("DE-REGISTERED"); //set status as de-register
						System.out.println("De-Registration accepted");
						System.out.println("   STORAGE CONTENT   ");
						System.out.println(storage);
						ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
						sendUDP(bStream, clientR, ds, ip);
					} else {
						// nothing happens just send back the class info
						clientR.setClientStatus(null);
						System.out.println("De-Registration rejected, User was not registered at this moment");
						ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
						sendUDP(bStream, clientR, ds, ip);
					}
				} else if (clientR.getRequest().equals("UPDATE")) {
					if (storage.containsKey(Key) == true) {
						storage.get(Key);
						b.add(data);
						storage.put(Key, b); // add to hashmap
						b = null; // to be used to point to other places in hashmap arrayList
						System.out.println(storage);
						System.out.println("Update done ");
						clientR.setClientStatus("UPDATE-CONFIRMED"); //set status as de-register
						System.out.println("   STORAGE CONTENT   ");
						System.out.println(storage);
						ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
						sendUDP(bStream, clientR, ds, ip);
					} else {
						System.out.println("Update denied: Name does not exist");
						clientR.setClientStatus("UPDATE-DENIED");
					}
				} /*else if (clientR.equals("SUBJECTS")) {
					for (int i = 0; i < 10; i++) { //loop through previous registered names
						if (clientR.gettClienName() == clientsReader.readLine()) {
							System.out.println("Subjects updated");
							clientR.setClientStatus("SUBJECTS-UPDATED");
						} else {
							System.out.println("Subjects rejected: Name or Subject does not exist");
							clientR.setClientStatus("SUBJECTS-REJECTED");
						}
					}
				} else if (clientR.equals("PUBLISH")) {
					for (int i = 0; i < 10; i++) { //loop through previous registered names
						if (clientR.gettClienName() == clientsReader.readLine()) {
							System.out.println("Message");
							clientR.setClientStatus("MESSAGE");
						} else if {
        			System.out.println("Publish denied: Name/Subject does not exist");
        			clientR.setClientStatus("PUBLISH-DENIED");
        		} else {
        			System.out.println("Publish denied: Subject is not in the user's interests");
        			clientR.setClientStatus("PUBLISH-DENIED");
        		}
					}
				} */else if (clientR.toString().equals("EXIT")) {
					System.out.println("Client sent exit... exiting");
					break;
				}
			}
		} catch	(IOException e) {
			System.err.println("IO exception in Client Handler");
			System.err.println(e.getStackTrace());
		}
    } // end of run

	public static void sendUDP(ByteArrayOutputStream b, RSS s, DatagramSocket ds, InetAddress ip) {
		try
		{
			ObjectOutput oo = new ObjectOutputStream(b);
			oo.writeObject(s);
			oo.close();
			byte[] serializedMessage = b.toByteArray();
			System.out.println("Sending BACK : " + s.gettClientSocket());
			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length,ip,s.gettClientSocket());
			ds.send(dpSend);
		}
		catch (IOException ex)
		{ex.printStackTrace(); }
	} // end of sendUDP
} // end of class ClientHandler