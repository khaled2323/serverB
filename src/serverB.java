import javax.xml.crypto.Data;
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
	private static boolean isServing = false;
	private static int serverSocket = 3051;
	private static int serverASocket = 3050;

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
		DatagramSocket ds = new DatagramSocket(serverSocket);
		ByteArrayOutputStream bStream;
		ObjectOutput objectOutput;
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;
		InetAddress ip = InetAddress.getLocalHost();
		Object inputObject;
		String key;
		String data;
		//InetAddress ip = InetAddress.getByName("8.8.8.8");

		//ClientHandler clientHandler = new ClientHandler(ds);

		while (true) {
			System.out.println("[SERVER] Waiting for a client connection...");

			// waiting for Server A to stop serving and receive Server A's client updates
			while (!isServing) {
				ClientHandler clientHandler = new ClientHandler(ds, serverASocket);
				clientHandler.run();
				isServing = clientHandler.getIsServing();
			} // end of while loop


			// Server B starts serving clients
			ServerTimer serverTimer = new ServerTimer(ds, serverASocket);
			Timer timer = new Timer();
			// timer starts
			System.out.println("Server started serving at: " + new Date());
			timer.schedule(serverTimer, 0);

			// timer ends
			Thread.sleep(90000); // 5min = 300,000ms
			timer.cancel();
			System.out.println("\nServer stopped serving at: " + new Date());
			String status = "This server is no longer serving, the other server must take over.\n";
			System.out.println(status + "   Storage Content:   \n" + ClientHandler.storage);

			// tell Server A to take over
			RSS serverA = new RSS(serverASocket);
			serverA.setClientStatus("CHANGE-SERVER");

			bStream = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(serverA);
			objectOutput.close();

			byte[] serializedMessage = bStream.toByteArray();
			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, serverASocket);
			ds.send(dpSend);

			// send Server A's socket and ip to all the clients
			bStream = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(serverA);
			objectOutput.close();

			serializedMessage = bStream.toByteArray();

			String string;
			String clientSocket = null;
			String clientIP = null;

			for (Map.Entry<String, ArrayList<String>> clientInfo : ClientHandler.storage.entrySet()) {
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
			// Server B stops serving
			isServing = false;
		}
	} // end of main
} // end of class ServerA

class ServerTimer extends TimerTask {
	DatagramSocket datagramSocket = null;
	int serverASocket;
	private static boolean isServing;
	private static ArrayList<ClientHandler> clients = new ArrayList<>();
	private static ExecutorService pool = Executors.newFixedThreadPool(4); // can increase this # depending on # of clients
	private volatile boolean exit = false;

	public ServerTimer(DatagramSocket ds, int serverASocket) {
		this.datagramSocket = ds;
		this.serverASocket = serverASocket;
	}

	public ServerTimer(DatagramSocket ds, int serverASocket, boolean isServing) {
		this.datagramSocket = ds;
		this.serverASocket = serverASocket;
		this.isServing = isServing;
	}

	@Override
	public void run() {
		// create ClientHandler threads to handle each client
		//while (!exit) {
			ClientHandler clientThread = null;
			try {
				clientThread = new ClientHandler(datagramSocket, serverASocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			clients.add(clientThread);
			pool.execute(clientThread);
		//} // end of while loop
	} // end of run

	public void stop() {
		exit = true;
	}
} // end of class serverTimer

