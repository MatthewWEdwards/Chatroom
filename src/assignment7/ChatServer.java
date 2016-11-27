/* Chat Room ChatServer.java
 * EE422C Project 7 submission by
 * Regan Stehle
 * rms3762
 * 16465
 * Matthew Edwards
 * mwe295
 * 16475
 * Slip days used: <0>
 * Fall 2016
 */

package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChatServer {
	private Map<Integer, PrintWriter> streams = new HashMap<Integer, PrintWriter>();
	private ArrayList<PrintWriter> clientOutputStreams;
	private ArrayList<ChatUser> userList = new ArrayList<ChatUser>();
	//TODO: make it so the server tells the client what the signaling char is
	private static final String signalingChar = "~"; // used to transmit commands to the server
	
	public static void main(String[] args) {
		try {
			new ChatServer().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		clientOutputStreams = new ArrayList<PrintWriter>();
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		while (true) {
			Socket clientSocket = serverSock.accept();
			PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			clientOutputStreams.add(writer);
			streams.put(Integer.valueOf(clientSocket.getPort()), writer);
			Thread t = new Thread(new ClientHandler(clientSocket));
			t.start();
			System.out.println("got a connection");
		}

	}

	private void notifyClients(String message, Socket sock) {
		String.valueOf(sock.getPort());
		for (ChatUser u : userList) {
			if(u.getOnlineStatus()){
				u.getOutputStream().println(String.valueOf(sock.getPort()) + " - " + message);
				u.getOutputStream().flush();
			}
		}
	}

	class ClientHandler implements Runnable {
		private BufferedReader reader;
		Socket sock;

		public ClientHandler(Socket clientSocket) throws IOException {
			sock = clientSocket;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if(message.length() > 0 && message.charAt(0) == signalingChar.charAt(0) ){
						command(message);
					}
					else{
						System.out.println("read " + message);
						notifyClients(message, sock);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void command(String message){
			String username = new String();
			String password = new String();
			switch(message.substring(1, message.indexOf(" "))){			
				case "login":		
					username = message.substring(message.indexOf(" ")+1, message.lastIndexOf(" "));
					password = message.substring(message.lastIndexOf(" ")+1, message.length());
					
					for(ChatUser u: userList){
						if(u.getUsername().equals(username) 
						   && u.getPassword().equals(password)){
						   if(u.getOnlineStatus()){
							   //TODO: already online protocol
							   return;
						   } else{
							   u.setOutputStream(streams.get(sock.getPort()));
							   u.setPort(sock.getPort());
							   u.setOnlineStatus(true);
							   return;
						   }
						}
					}
					
					break;
					
				case "register":
					username = message.substring(message.indexOf(" ")+1, message.lastIndexOf(" "));
					password = message.substring(message.lastIndexOf(" ")+1, message.length());
					ChatUser toAdd = new ChatUser(username, password);
					for(ChatUser u: userList){
						if(u.equals(toAdd)){
							//TODO: user exists protocol
							return;
						}
					}
					userList.add(toAdd);
			}
		}
	}

}
