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
import java.io.OutputStream;
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
	// TODO: make it so the server tells the client what the signaling char is
	private static String signalingChar = "~"; // used to transmit commands to the server

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
			if (u.getOnlineStatus()) {
				u.getWriter().println(String.valueOf(sock.getPort()) + " - " + message);
				u.getWriter().flush();
			}
		}
	}

	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private Socket sock;
		private PrintWriter sockWriter;

		public ClientHandler(Socket clientSocket) throws IOException {
			sock = clientSocket;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			sockWriter = new PrintWriter(sock.getOutputStream());
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if (message.length() > 0 && message.charAt(0) == signalingChar.charAt(0)) {
						command(message);
					} else {
						System.out.println("read " + message);
						notifyClients(message, sock);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * execute commands sent by the client to the server
		 * 
		 * @param message:
		 *            The message sent by the client to the server.
		 */
		public void command(String message) {
			String username = new String();
			String password = new String();
			switch (message.substring(1, message.indexOf(" "))) {

			// Login to the chat server, or tell the user the username
			// doesn't exist, or is already logged on
			case "login":
				username = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
				password = message.substring(message.lastIndexOf(" ") + 1, message.length());
				for (ChatUser u : userList) {
					if (u.getUsername().equals(username)) {
						if(!(u.getPassword().equals(password))){
							sockWriter.println(signalingChar + "passwordMismatch ");
							sockWriter.flush();
							return;
						}
						if (u.getOnlineStatus()) {
							sockWriter.println(signalingChar + "alreadyOnline ");
							sockWriter.flush();
							return;
						} else {
							u.setWriter(sockWriter);
							u.setPort(sock.getPort());
							u.setOnlineStatus(true);
							u.getWriter().println(signalingChar + "login ");
							u.getWriter().flush();
							return;
						}
					}
				}
				sockWriter.println(signalingChar + "noUser ");
				sockWriter.flush();
				break;

			// Create a new user, or tell the client a username is taken
			case "register":
				username = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
				password = message.substring(message.lastIndexOf(" ") + 1, message.length());
				ChatUser toAdd = new ChatUser(username, password);
				for (ChatUser u : userList) {
					if (u.equals(toAdd)) {
						sockWriter.println(signalingChar + "registerError ");
						sockWriter.flush();
						return;
					}
				}
				userList.add(toAdd);
				break;

			// Do nothing
			default:
				return;
			}
		}
	}

}
