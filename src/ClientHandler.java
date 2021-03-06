import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
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
			System.out.println("Now in publications " + publications);
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

				clientHandler:
				{
					if (clientR.getClientStatus().equals("FIRST-REQUEST")) {
						// do nothing
						break clientHandler;
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
					} else if (clientR.getRequest().equals("DE-REGISTER")) {
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
						}
						// user name doesn't exist
						else if (storage.containsKey(Key) == false){
							// nothing happens just send back the class info
							clientR.setClientStatus("DE-REGISTER-DENIED");
							clientR.setReason("De-Registration rejected, User was not registered at this moment");
							System.out.println(clientR.getReason());

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
						}
						// user name doesn't exist
						else if (storage.containsKey(Key) == false){
							// nothing happens just send back the class info
							clientR.setClientStatus("UPDATE-DENIED");
							clientR.setReason("Name does not exist");
							System.out.println(clientR.getReason());
						}

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

					// if client request is SUBJECTS
					else if(clientR.getRequest().equals("SUBJECTS")) {
						// if client name is in storage
						if (storage.containsKey(Key)) {
							// client does not exist in storageSubjects
							if (!storageSubjects.containsKey(Key)) {
								// check if subjects are in possible subjects
								ArrayList<String> clientRSubjects = clientR.getSubjects();
								// if client's subject(s) are not one of the possible subjects
								for (int i = 0; i < clientRSubjects.size(); i++) {
									if (Arrays.stream(possibleSubjects).noneMatch(clientRSubjects.get(i)::equals)) {
										String reason = "Subjects update denied: " + clientRSubjects.get(i) + " is not a possible subject";
										System.out.println(reason);
										clientR.setReason(reason);
										clientR.setClientStatus("SUBJECTS-REJECTED"); //set status as de-register
										break;
									} else if (Arrays.asList(possibleSubjects).contains(clientRSubjects.get(i))){
										// add key and content of arrayList
										storageSubjects.put(Key, clientR.getSubjects());

										clientR.setClientStatus("SUBJECTS-UPDATED"); //set status as de-register
										clientR.setSubjects(clientR.getSubjects());

										for (String clientRSubject : clientRSubjects) {
											ArrayList<String> m = new ArrayList<String>();
											publications.put(clientRSubject, m);
											m = null;
										}
									}
								}
							}

							// name already exists in subjects storage
							else if (storageSubjects.containsKey(Key)) {
								ifFromServer:
								{
									if (clientR.from().equals("SERVER")) {
										ArrayList<String> clientRSubjects = clientR.getSubjects();

										if (clientR.getClientStatus().equals("SUBJECTS-UPDATED")) {
											storageSubjects.put(Key, clientR.getSubjects());

											for (String clientRSubject : clientRSubjects) {
												ArrayList<String> m = new ArrayList<String>();
												publications.put(clientRSubject, m);
												m = null;
											}
											break ifFromServer;
										}
									}

									// check if client already has these subjects as interests, otherwise add them
									boolean hasSubjects = false;
									boolean notASubject = false;
									ArrayList<String> clientRSubjects = clientR.getSubjects();
									ArrayList<String> subjectsInStorage = storageSubjects.get(clientR.gettClienName());
									System.out.println("Client " + clientR.gettClienName() + "'s current subject(s) of interest: " + subjectsInStorage);

									checkSubjects:
									{
										for (int i = 0; i < subjectsInStorage.size(); i++) {
											for (int j = 0; j < clientRSubjects.size(); j++) {
												if (Arrays.stream(possibleSubjects).noneMatch(clientRSubjects.get(i)::equals)) {
													String reason = "Subjects update denied: " + clientRSubjects.get(i) + " is not a possible subject";
													System.out.println(reason);
													clientR.setReason(reason);
													clientR.setClientStatus("SUBJECTS-REJECTED"); //set status as de-register
													notASubject = true;
													break checkSubjects;
												}

												else if (subjectsInStorage.get(i).equals(clientRSubjects.get(j))) {
													String reason = "Subjects update denied: Client already has subject " + clientRSubjects.get(j);
													clientR.setReason(reason);
													System.out.println(reason);

													hasSubjects = true;
													break checkSubjects;
												}
											}
										}
									} // end of checkSubjects

									if (hasSubjects || notASubject)
										clientR.setClientStatus("SUBJECTS-REJECTED"); //set status as SUBJECTS-REJECTED									}

										// else if client doesn't already have subjects as interests, add them
									else if (!hasSubjects && !notASubject) {
										// add key and content of arrayList
										for (int j = 0; j < clientRSubjects.size(); j++) {
											subjectsInStorage.add(clientRSubjects.get(j));
										}
										storageSubjects.put(Key, subjectsInStorage);

										clientR.setClientStatus("SUBJECTS-UPDATED"); //set status as de-register
										clientR.setSubjects(subjectsInStorage);

										for(int i=0; i < subjectsInStorage.size(); i++) {
											ArrayList<String> m = new ArrayList<String>();
											String subjectKey = subjectsInStorage.get(i);
											publications.put(subjectKey, m);
										}
									}
								}
							}
						}
						// client name is not in storage
						else if (storage.containsKey(Key) == false){
							String reason = "Subjects adding denied: Name does not exist";
							System.out.println(reason);
							clientR.setClientStatus("SUBJECTS-REJECTED");
							clientR.setReason(reason);
						}

						// if receiving from client, send back to client and other server
						if (clientR.from().equals("CLIENT")) {
							clientR.setFrom("SERVER");
							clientR.setServerSocket(serverASocket);
							System.out.println("Subjects Storage: " + storageSubjects);
							ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
							ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
							sendUDP(bStream, clientR, ds, ip);
							sendUDPToServer(bStream2, clientR, ds, ip);
							clientR.setSubjects(null);
						}
						else if (clientR.from().equals("SERVER")) {
							System.out.println("Subjects Storage: " + storageSubjects);
						}
					} // end of SUBJECTS request

					// if client request is PUBLISH
					else if(clientR.getRequest().equals("PUBLISH")) {
						if (storage.containsKey(Key) == true) {
							// user is already registered
							if(publications.containsKey(clientR.getsubject())) {
								// is one of client's subject(s) of interests
								System.out.println("Client Subject: " + clientR.getsubject());
								System.out.println("Client MESSAGE: " + clientR.getMessage());
								ArrayList<String> m;
								m = publications.get(clientR.getsubject());
								//System.out.println("getPublications to be added " + publications.get(clientR.getsubject()));
								m.add(clientR.getMessage());
								publications.put(clientR.getsubject(), m);
								System.out.println("Publication Accepted: "+ publications);

								// check all users who registered for this subject
								int co = 0;
								for (Map.Entry<String, ArrayList<String>> clientInfo : storage.entrySet()) {
									ArrayList<String> currentList = clientInfo.getValue();
									String n = clientInfo.getKey();
									// iterate on the current list to get the data to send to clients
									co++;
									String d = currentList.get(0);
									String[] splitInfo = d.split(" ");
									String O = splitInfo[1];
									String clientIP = splitInfo[2];
									String clientSocket = splitInfo[3];
									String s = clientR.getsubject();

									// check the clients subjects
									ArrayList<String> t = storageSubjects.get(n);

									//subject name
									for(int k=0; k<t.size();k++) {
										// search array list of current client
										if(t.get(k).equals(clientR.getsubject())) {
											String ms = publications.get(clientR.getsubject()).toString();
											RSS client = new RSS(n,Integer.parseInt(clientSocket),Integer.parseInt(O),ip,clientR.getRequest());
											client.setMessage(ms);
											client.setClientStatus("MESSAGE");

											// sends to other server
											ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
											ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
											client.setServerSocket(serverASocket);
											client.setFrom("SERVER");
											client.setClientSimulationIp(clientR.getClientSimulationIp());
											sendUDPToServer(bStream2, client, ds, ip);
											client.setSubject(clientR.getsubject());
											sendUDP(bStream, client, ds, ip);
										} // end of inner for loop
									} // end of outer for loop
								} // end of outer outer for loop
							} // end of if existing subject

							// not an existing subject
							else if (!publications.containsKey(clientR.getsubject())) {
								String reason = "Publish Denied: Not Client's Subject(s) of Interest";
								System.out.println(reason);
								clientR.setClientStatus("PUBLISH-DENIED");
								clientR.setReason(reason);

								if (clientR.from().equals("CLIENT")) {
									// sends to other server
									clientR.setFrom("SERVER");
									clientR.setServerSocket(serverASocket);
									ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
									ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
									sendUDP(bStream, clientR, ds, ip);
									clientR.setServerSocket(serverASocket);
									clientR.setFrom("SERVER");
									sendUDPToServer(bStream2, clientR, ds, ip);
								}
							}
						} // end of if name exists

						else if (storage.containsKey(Key) == false) {
							String reason = "Publish denied: Name does not exist";
							System.out.println(reason);
							clientR.setClientStatus("PUBLISH-DENIED");
							clientR.setReason(reason);


							if (clientR.from().equals("CLIENT")) {
								// sends to other server
								clientR.setFrom("SERVER");
								clientR.setServerSocket(serverASocket);
								ByteArrayOutputStream bStream = new ByteArrayOutputStream(); //sendback class info to user
								ByteArrayOutputStream bStream2 = new ByteArrayOutputStream();
								sendUDP(bStream, clientR, ds, ip);
								clientR.setServerSocket(serverASocket);
								clientR.setFrom("SERVER");
								sendUDPToServer(bStream2, clientR, ds, ip);
							}
						}
					}

					else if (clientR.toString().equals("EXIT")) {
						System.out.println("Client sent exit... exiting");
						break;
					}
				}
			} // end of clientHandler
			} catch(IOException e){
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