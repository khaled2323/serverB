import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	public static void main(String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		int serverSocket = 3051;
		DatagramSocket ds = new DatagramSocket(serverSocket);
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;
		InetAddress ip = InetAddress.getLocalHost();
		Object inputObject;
		int serverASocket = 3050;
		//InetAddress ip = InetAddress.getByName("8.8.8.8");

		ClientHandler clientHandler = new ClientHandler(ds);

		while (true) {
			System.out.println("[SERVER] Waiting for a client connection...");

			// waiting for Server A to stop serving, and to receive Server A's storage
			while (true) {
				DpReceive = new DatagramPacket(receive, receive.length);

				ds.receive(DpReceive);

				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receive));

				inputObject = iStream.readObject();
				if (inputObject instanceof HashMap) {
					System.out.println("Storage from Server A:\n" + inputObject); // used for testing
					storage = (HashMap) inputObject;
					break;
				}
			} // end of while loop

			ServerTimer serverTimer = new ServerTimer(ds);
			Timer timer = new Timer();
			// timer starts
			System.out.println("Server started serving at: " + new Date());
			timer.schedule(serverTimer, 0);

			// timer ends
			Thread.sleep(20000); // 5min = 300,000ms
			System.out.println("\nServer stopped serving at: " + new Date());
			String status = "This server is no longer serving, the other server must take over.\n";
			System.out.println(status + "   Storage Content:   \n" + clientHandler.storage);

			// sends Server B's storage to Server A
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutput objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(clientHandler.storage);
			objectOutput.close();

			byte[] serializedMessage = bStream.toByteArray();

			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, serverASocket);
			ds.send(dpSend);

			// send Server A's socket and ip to all the clients
			RSS serverA = new RSS(serverASocket);
			serverA.setClientStatus("CHANGE-SERVER");

			bStream = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(serverA);
			objectOutput.close();

			serializedMessage = bStream.toByteArray();

			String string;
			String clientSocket = null;
			String clientIP = null;

			for (Map.Entry<String, ArrayList<String>> clientInfo : clientHandler.storage.entrySet()) {
				ArrayList<String> currentList = clientInfo.getValue();

				// iterate on the current list
				for (int j = 0; j < currentList.size(); j++) {
					string = currentList.get(0);
					String[] splitInfo = string.split(" ");
					clientIP = splitInfo[2];
					clientSocket = splitInfo[3];
				}
				dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, Integer.parseInt(clientSocket));
				ds.send(dpSend);
			}
		}
	} // end of main
} // end of class ServerA

class ServerTimer extends TimerTask {
	DatagramSocket datagramSocket = null;
	private static ArrayList<ClientHandler> clients = new ArrayList<>();
	private static ExecutorService pool = Executors.newFixedThreadPool(4); // can increase this # depending on # of clients

	public ServerTimer(DatagramSocket ds) {
		this.datagramSocket = ds;
	}

	@Override
	public void run() {
		//randomTime = r.nextInt(20000);

		// create ClientHandler threads to handle each client
		//while (true) {
		ClientHandler clientThread = null;
		try {
			clientThread = new ClientHandler(datagramSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clients.add(clientThread);
		pool.execute(clientThread);
		//} // end of while loop
	}
} // end of class serverTimer

