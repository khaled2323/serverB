import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class serverB {
	static HashMap<String, ArrayList<String>> storage = new HashMap<String, ArrayList<String>>();
	public static StringBuilder data(byte[] a) {
		// TODO Auto-generated method stub
		if(a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		
		while(a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}

	
	public static void sendUDP(ByteArrayOutputStream b, RSS s,DatagramSocket ds,InetAddress ip  ) {
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
	}
	
	public static void main(String [] args) throws IOException {
		DatagramSocket ds = new DatagramSocket(3051);
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;
		InetAddress ip = InetAddress.getLocalHost();
		//InetAddress ip = InetAddress.getByName("8.8.8.8");
		RSS clientR = null;
		
		while(true) {
			System.out.println("\nServer Listening to Client: " + ip.toString() + ":" + ds.getPort());

			DpReceive = new DatagramPacket(receive, receive.length);
			
			ds.receive(DpReceive);
			
			ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receive));
	
			try {
				clientR = (RSS) iStream.readObject();
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			iStream.close();
			System.out.println("Client Name: " + clientR.gettClienName());
			System.out.println("Client Request: " + clientR.getRequest());
			System.out.println("Order # : " + clientR.getOrderNumber());
			System.out.println("Client IP: " + clientR.getClientSimulationIp());
			System.out.println("Client Socket Number: " + clientR.gettClientSocket());
			String Key;
			String data;
			Key = clientR.gettClienName();
			data = clientR.getRequest() + " "+clientR.getOrderNumber()+ " "+clientR.getClientSimulationIp()+ " "+clientR.gettClientSocket();
			ArrayList<String> b = new ArrayList<String>();
			if(clientR.getRequest().equals("REGISTER")) {
				// TODO create condition to check registration success
				if(storage.containsKey(Key)== false) {
					
					System.out.println("A Register request is made by "+ clientR.gettClienName());
					// save data
					b.add(data);
					storage.put(Key,b); // add to hashmap
					b = null; // to be used to point to other places in hashmap arrayList
					// send back confirmation
					System.out.println("Registration accepted");
					clientR.setClientStatus("REGISTERED");
					System.out.println(" ");
					System.out.println("*****************************************************");
					System.out.println("   STORAGE CONTENT   ");
					System.out.println(storage);
					ByteArrayOutputStream bStream = new ByteArrayOutputStream();
					sendUDP(bStream,clientR,ds,ip);
				}
				else if(storage.containsKey(Key)== true) {
					//System.out.println( " status currently " + clientR.getClientStatus());
				clientR.setClientStatus("REGISTER-DENIED");
				clientR.setReason("User Already Registered");
				System.out.println("REGISTER-DENIED");
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				sendUDP(bStream,clientR,ds,ip);
				}
			}
			else if(clientR.getRequest().equals("DE-REGISTER")) {
				if(storage.containsKey(Key)== true) { //if user data exists
					storage.remove(Key); //delete from hashmap
					clientR.setClientStatus("DE-REGISTERED"); //set status as de-register
					System.out.println("de-Registration accepted");
					ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
					sendUDP(bStream,clientR,ds,ip);
				}else {
					// nothing happens just send back the class info
					clientR.setClientStatus(null);
					System.out.println("Registration rejected, User was not registered at this moment");
					ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
					sendUDP(bStream,clientR,ds,ip);
				}
			}else if(clientR.getRequest().equals("UPDATE")) {
				if(storage.containsKey(Key)== true) {
					storage.get(Key);
					b.add(data);
					storage.put(Key,b); // add to hashmap
					b = null; // to be used to point to other places in hashmap arrayList
					System.out.println(storage);
					System.out.println("Update done ");
					clientR.setClientStatus("UPDATE-CONFIRMED"); //set status as de-register
					ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
					sendUDP(bStream,clientR,ds,ip);
				}
			}
			if(data(receive).toString().equals("EXIT")) {
				System.out.println("Client sent exit... exiting");
				break;
			}
			
			//receive = new byte[65535];
		}
	}

	
}