import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ClientHandler implements Runnable {
	private DatagramSocket ds;
	private int serverASocket = 3050;
	private boolean isServing = false;
	static HashMap<String, ArrayList<String>> storage = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> storageSubjects = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> publications = new HashMap<String, ArrayList<String>>();
	private String possibleSubjects[]= {"ENGR201","ENGR202","ENGR213","ENGR233","ENGR290","ENGR301","ENGR371","ENGR391","ENGR392","COEN212","COEN243","COEN244","COEN311","COEN313","COEN316","COEN317","COEN352","COEN346","COEN320","COEN445","COEN390","COEN490","SOEN341","ELEC273","ELEC242","ELEC321","ELEC342","ELEC311","ELEC372","ELEC353","COEN433","COEN413"};

	RSS clientR;
	byte[] receive = new byte[65535];
	DatagramPacket DpReceive = null;
	InetAddress ip = InetAddress.getLocalHost();
	//InetAddress ip = InetAddress.getByName("8.8.8.8");

	public ClientHandler(DatagramSocket clientSocket, int serverASocket) throws IOException {
        this.ds = clientSocket;
        this.serverASocket = serverASocket;
    }

	public boolean getIsServing() {
		return this.isServing;
	}

	@Override
    public void run() {
		try {
			for(int i=0; i < possibleSubjects.length; i++) {
				ArrayList<String> m = new ArrayList<String>();
				String subjectKey = possibleSubjects[i];
				publications.put(subjectKey, m);
				m = null;
			}
			System.out.println("Now in publications "+ publications);
			while (true) {
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

				if (clientR.getClientStatus() != null && clientR.getClientStatus().equals("CHANGE-SERVER")) {
					isServing = true;
					break;
				}

				// print which client the server thread is listening to
				if (clientR.from().equals("CLIENT")) {
					System.out.println("\nServer Listening to Client: " + clientR.getClientSimulationIp() + ":" + clientR.gettClientSocket());
				}

				System.out.println("\nClient Name: " + clientR.gettClienName());
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

						// sends to other server
						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream();
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
						}
					} else if (storage.containsKey(Key) == true) {
						//System.out.println( " status currently " + clientR.getClientStatus());
						clientR.setClientStatus("REGISTER-DENIED");
						clientR.setReason("User Already Registered");
						System.out.println("REGISTER-DENIED");


						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream();
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
						}
					}
				}

				else if (clientR.getRequest().equals("DE-REGISTER")) {
					if (storage.containsKey(Key) == true) { //if user data exists
						storage.remove(Key); //delete from hashmap
						clientR.setClientStatus("DE-REGISTERED"); //set status as de-register
						System.out.println("De-Registration accepted");
						System.out.println("   STORAGE CONTENT   ");
						System.out.println(storage);

						// sends to other server
						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
						}
					} else {
						// nothing happens just send back the class info
						clientR.setClientStatus(null);
						System.out.println("De-Registration rejected, User was not registered at this moment");

						// sends to other server
						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
						}
					}
				}

				else if (clientR.getRequest().equals("UPDATE")) {
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

						// sends to other server
						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
						}
					} else {
						System.out.println("Update denied: Name does not exist");
						clientR.setClientStatus("UPDATE-DENIED");
					}
				}

				// NEED TO ADD IF ELSE CONDITIONS FOR possible SUBJECTS
				
				else if(clientR.getRequest().equals("SUBJECTS")) { //1 else for subject
					for (int i = 0; i < possibleSubjects.length; i++) {
						if (possibleSubjects[i] == clientR.getsubject()) {
							System.out.println("TEST - client status: " + clientR.getClientStatus());
							if (storage.containsKey(Key) == true) {
								if (storageSubjects.containsKey(Key) == false) {
									// add key and content of arrayLis
									storageSubjects.put(Key, clientR.getSubjects());
									System.out.println("Subjects recieved by: "+ Key);
									clientR.setClientStatus("SUBJECT-UPDATED"); //set status as de-register
									System.out.println(storageSubjects);
								
									// sends to other server
									if (clientR.from().equals("CLIENT")) {
										clientR.setFrom("SERVER");
										clientR.setServerSocket(serverASocket);
										ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
										ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
										sendUDP(bStream, clientR, ds, ip);
										sendUDPToServer(bStream2, clientR, ds, ip);
										clientR.setSubjects(null);
									}

								} else {
									// NAME ALREADY EXISTS
									storageSubjects.put(Key, clientR.getSubjects());
									System.out.println("Subjects recieved by :"+ Key);
									clientR.setClientStatus("SUBJECT-UPDATED"); //set status as de-register
									System.out.println(storageSubjects);

									// sends to other server
									if (clientR.from().equals("CLIENT")) {
										clientR.setFrom("SERVER");
										clientR.setServerSocket(serverASocket);
										ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
										ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
										sendUDP(bStream, clientR, ds, ip);
										sendUDPToServer(bStream2, clientR, ds, ip);
										clientR.setSubjects(null);
									}
								}
							} else {
								String reason = "Subjects adding denied: Name does not exist";
								System.out.println(reason);
								clientR.setClientStatus("SUBJECTS-REJECTED");
								clientR.setReason(reason);

								// sends to other server
								if (clientR.from().equals("CLIENT")) {
									clientR.setFrom("SERVER");
									clientR.setServerSocket(serverASocket);
									ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
									ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
									sendUDP(bStream, clientR, ds, ip);
									sendUDPToServer(bStream2, clientR, ds, ip);
								}
							}
						} 
					}
					// PROBLEM HERE RECEIVING PUBLISH FROM OTHER SERVER
					// NEED TO ADD IF ELSE CONDITIONS
				} else if(clientR.getRequest().equals("PUBLISH")) { // 3 else
					if (storage.containsKey(Key) == true) {
						// user is already registered
						if(publications.containsKey(clientR.getsubject())) {
							// subject exists in publications
							System.out.println("Client Subject: " + clientR.getsubject());
							System.out.println("Client MESSAGE: " + clientR.getMessage());
							ArrayList<String> m;
							m = publications.get(clientR.getsubject());
							//System.out.println("getPublications to be added " + publications.get(clientR.getsubject()));
							m.add(clientR.getMessage());
							publications.put(clientR.getsubject(), m);
							System.out.println("publication accepted : "+ publications);

							// check all users who registered for this subject
							int co=0;
							for (Map.Entry<String, ArrayList<String>> clientInfo : storage.entrySet()) {
								ArrayList<String> currentList = clientInfo.getValue();
								String n = clientInfo.getKey();
								// iterate on the current list to get the data to send to clients
								System.out.println("c0" + co+ "check name now "+ n);
								co++;
								String d = currentList.get(0);
								String[] splitInfo = d.split(" ");
								String O = splitInfo[1];
								String clientIP = splitInfo[2];
								String clientSocket = splitInfo[3];
								String s = clientR.getsubject();

								// check the clients subjects
								ArrayList<String> t = storageSubjects.get(n);
								System.out.println("arralist size " +t.size());

								//subject name
								for(int k=0; k<t.size();k++) {
									// search array list of current client
									if(t.get(k).equals(clientR.getsubject())) {
										System.out.println(t.get(k)+publications.get(clientR.getsubject()));
										//set their values
										//ArrayList <String> msg = publications.get(clientR.getsubject());

										String ms = publications.get(clientR.getsubject()).toString();
										RSS client = new RSS(n,Integer.parseInt(clientSocket),Integer.parseInt(O),ip,clientR.getRequest());
										client.setMessage(ms);
										client.setClientStatus("MESSAGE");

										// sends to other server
										clientR.setFrom("SERVER");
										clientR.setServerSocket(serverASocket);
										ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
										ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
										sendUDP(bStream, client, ds, ip);
										sendUDPToServer(bStream2, clientR, ds, ip);
										//sendUDP
									} else {
										String reason = "Subjects adding denied: User not registered to subject";
										System.out.println(reason);
										clientR.setClientStatus("PUBLISH-DENIED");
										clientR.setReason(reason);

										// sends to other server
										if (clientR.from().equals("CLIENT")) {
											clientR.setFrom("SERVER");
											clientR.setServerSocket(serverASocket);
											ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
											ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
											sendUDP(bStream, clientR, ds, ip);
											sendUDPToServer(bStream2, clientR, ds, ip);
										}
									}
								} // end of inner for loop
							} // end of outter for loop
						} else {
							String reason = "Subjects adding denied: Subject does not exist";
							System.out.println(reason);
							clientR.setClientStatus("PUBLISH-DENIED");
							clientR.setReason(reason);

							// sends to other server
							if (clientR.from().equals("CLIENT")) {
								clientR.setFrom("SERVER");
								clientR.setServerSocket(serverASocket);
								ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
								ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
								sendUDP(bStream, clientR, ds, ip);
								sendUDPToServer(bStream2, clientR, ds, ip);
							}
						} // end of if statement
					} else {
						String reason = "Subjects adding denied: Name does not exist";
						System.out.println(reason);
						clientR.setClientStatus("PUBLISH-DENIED");
						clientR.setReason(reason);

						// sends to other server
						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
						}
					}
				} else if (clientR.toString().equals("EXIT")) {
					System.out.println("Client sent exit... exiting");
					break;
				}
			}
	} catch	(IOException e) {
			System.err.println("IO exception in Client Handler");
			System.err.println(e.getStackTrace());
		}
    } // end of call

	public static void sendUDP(ByteArrayOutputStream b, RSS s, DatagramSocket ds, InetAddress ip) {
		try
		{
			ObjectOutput oo = new ObjectOutputStream(b);
			oo.writeObject(s);
			oo.close();
			byte[] serializedMessage = b.toByteArray();
			System.out.println("Sending BACK: " + s.gettClientSocket());
			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, s.gettClientSocket());
			ds.send(dpSend);
		}
		catch (IOException ex)
		{ex.printStackTrace(); }
	} // end of sendUDP

	public static void sendUDPToServer(ByteArrayOutputStream b, RSS s, DatagramSocket ds, InetAddress ip) {
		try
		{
			ObjectOutput oo = new ObjectOutputStream(b);
			oo.writeObject(s);
			oo.close();
			byte[] serializedMessage = b.toByteArray();
			System.out.println("Sending to other Server: " + s.getServerSocket());
			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, s.getServerSocket());
			ds.send(dpSend);
		}
		catch (IOException ex)
		{ex.printStackTrace(); }
	} // end of sendUDPToServer method
} // end of class ClientHandler