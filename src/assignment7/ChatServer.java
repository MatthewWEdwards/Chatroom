/* Chat Room ChatServer.java
 * EE422C Project 7 submission by
 * Regan Stehle
 * rms3762
 * 16465
 * Matthew Edwards
 * mwe295
 * 16475
 * Slip days used: <1>
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
import java.util.Scanner;

public class ChatServer {
	private ArrayList<ChatUser> userList = new ArrayList<ChatUser>();
	private ArrayList<ChatRoom> roomList = new ArrayList<ChatRoom>();
	

	public static void main(String[] args) {
		try {
			new ChatServer().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		while (true) {
			Socket clientSocket = serverSock.accept();
			Thread t = new Thread(new ClientHandler(clientSocket));
			t.start();
			System.out.println("got a connection");
		}

	}

	private void sendMessage(String message, Socket sock) {
		int port = Integer.valueOf(String.valueOf(sock.getPort()));
		for(ChatUser u: userList){
			if(u.getPort() == port){
				for(ChatRoom r: roomList){
					if(u.getChat().equals(r.toString())){
						r.getMessage(u.toString() + "> " + message);
						return;
					}
				}
				return;
			}
		}
		
		for (ChatUser u : userList) {
			if (u.getOnlineStatus()) {
				u.getWriter().println(port + " - " + message);
				u.getWriter().flush();
			}
		}
	}
	
	private void updateUserList(){
		String message = new String();
		message = "~updateUsers ";
		for (ChatUser u : userList) {
			if (u.getOnlineStatus()) {
				message += u.toString() + " ";
			}
		}
		for (ChatUser u : userList) {
			if (u.getOnlineStatus()) {
				u.getWriter().println(message);
				u.getWriter().flush();
			}
		}
	}
	
	private void updateRoomList(){
		String message = new String();
		message = "~updateRooms ";
		for (ChatRoom r : roomList) {
			message += r.toString() + " ";
		}
		for (ChatUser u : userList) {
			if (u.getOnlineStatus()) {
				u.getWriter().println(message);
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
					if (message.length() > 0 && message.charAt(0) == ApprovedChars.signalingChar.charAt(0)) {
						command(message);
					} else {
						System.out.println("read " + message);
						sendMessage(message, sock);
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
							sockWriter.println(ApprovedChars.signalingChar + "passwordMismatch ");
							sockWriter.flush();
							return;
						}
						if (u.getOnlineStatus()) {
							sockWriter.println(ApprovedChars.signalingChar + "alreadyOnline ");
							sockWriter.flush();
							return;
						} else {
							u.setWriter(sockWriter);
							u.setPort(sock.getPort());
							u.setOnlineStatus(true);
							u.getWriter().println(ApprovedChars.signalingChar + "login " + username);
							u.getWriter().flush();
							updateUserList();
							updateRoomList();
							return;
						}
					}
				}
				sockWriter.println(ApprovedChars.signalingChar + "noUser ");
				sockWriter.flush();
				break;

				
			case "logout":
				username = message.substring(message.indexOf(" ") + 1);
				for (ChatUser u : userList) {
					if (u.getUsername().equals(username)) {
						u.setOnlineStatus(false);
						updateUserList();
						return;
					}
				}
				
				break;
			// Create a new user, or tell the client a username is taken
			case "register":
				username = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
				password = message.substring(message.lastIndexOf(" ") + 1, message.length());
				ChatUser toAdd = new ChatUser(username, password);
				for (ChatUser u : userList) {
					if (u.equals(toAdd)) {
						sockWriter.println(ApprovedChars.signalingChar + "registerError ");
						sockWriter.flush();
						return;
					}
				}
				userList.add(toAdd);
				break;
				
			case "request":
				String curUser = new String();
				String potentialFriend = new String();
				potentialFriend = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
				curUser = message.substring(message.lastIndexOf(" ") + 1, message.length()); 
				boolean exist = false;
				if(potentialFriend.equals(curUser)){
					//same person
					sockWriter.println(ApprovedChars.signalingChar + "sameUser ");
					sockWriter.flush();
					return;
				}
					
				for (ChatUser u : userList) {
					if (u.getUsername().equals(potentialFriend)) {
						exist = true;
						//check if they're already friends, then either request sent or already friends
						ArrayList<String> friends = u.getFriends(); //friends of potentialFriend
						for(String s : friends){
							if (s.equals(curUser)){ //already friends
								sockWriter.println(ApprovedChars.signalingChar + "alreadyFriends ");
								sockWriter.flush();
								return;
							}
						}
						ArrayList<String> requests = u.getRequests();
						for(String s: requests ){
							if (s.equals(curUser)){
								sockWriter.println(ApprovedChars.signalingChar + "alreadyRequested ");
								sockWriter.flush();
								return;
							}
						}
						for (ChatUser c : userList) {
							if (c.getUsername().equals(curUser)) {
								requests = c.getRequests();
								for (String s : requests)
									if (s.equals(potentialFriend)){
										sockWriter.println(ApprovedChars.signalingChar + "doubleRequest ");
										sockWriter.flush();
										return;
									}
										
							} 
						}
						
						u.addToRequests(curUser);
						sockWriter.println(ApprovedChars.signalingChar + "requestSent ");
						sockWriter.flush();
						return;
						
					}
					
				}
				if(!exist){
					sockWriter.println(ApprovedChars.signalingChar + "noUser ");
					sockWriter.flush();
					return;
				}
									
				break;
			case "checkRequest":
				curUser = message.substring(message.indexOf(" ") + 1);
				for(ChatUser u : userList){
					if(u.getUsername().equals(curUser)){
						ArrayList<String> requests = u.getRequests();
						if(requests.size() == 0)
							return;
						sockWriter.println(ApprovedChars.signalingChar + "requestReturn " + requests);
						sockWriter.flush();
					}
						
				}
				break;
			case "makeChatRoom":

				String chatRoom = message.substring(message.indexOf(" ") + 1);
				Scanner sMakeChatRoom = new Scanner(chatRoom);
				String chatRoomName = sMakeChatRoom.next();
				String chatRoomowner = sMakeChatRoom.next();
				boolean chatRoomPublicPrivate = Boolean.valueOf(sMakeChatRoom.next());
				ChatRoom newChatRoom = new ChatRoom(chatRoomName, chatRoomowner, chatRoomPublicPrivate);
				for (ChatRoom r : roomList) {
					if (r.equals(newChatRoom)) {
						//TODO: room already exists
						sMakeChatRoom.close();
						return;
					}
				}
				roomList.add(newChatRoom);
				newChatRoom.addUser(chatRoomowner);
				sMakeChatRoom.close();
				updateRoomList();
				break;
				
			case "giveUserPermissions":
				String permissionsString = message.substring(message.indexOf(" ") + 1);
				Scanner sGiveUserPermissions = new Scanner(permissionsString);
				String permissionsChatRoom = sGiveUserPermissions.next();
				String supposedOwner = sGiveUserPermissions.next();
				String userToAdd = sGiveUserPermissions.next();
				
				for (ChatRoom r : roomList) {
					if (r.toString().equals(permissionsChatRoom)) {
						if(!(r.privateChat)){
							//TODO: send message about not private chat
							sGiveUserPermissions.close();
							return;
						}
						if(!supposedOwner.equals(r.owner)){
							//TODO: send message about not owner
							sGiveUserPermissions.close();
							return;
						}
						r.addUser(userToAdd);
						sGiveUserPermissions.close();
						return;
					}
				}
				sGiveUserPermissions.close();
				break;
				
			case "joinChat":
				String joinString = message.substring(message.indexOf(" ") + 1);
				Scanner sjoinChat = new Scanner(joinString);
				String toJoin = sjoinChat.next();
				String userToJoin = sjoinChat.next();
				String currentChatRoom = sjoinChat.next();
				ChatUser userObjToJoin = null;
				ChatRoom currentChatRoomObj = null;
				for(ChatUser u: userList){
					if(u.toString().equals(userToJoin)){
						userObjToJoin = u;
						break;
					}
				}
				for(ChatRoom r: roomList){
					if(currentChatRoom.toString().equals(r.toString())){
						currentChatRoomObj = r;
						break;
					}
				}
				for (ChatRoom r : roomList) {
					if(r.toString().equals(toJoin)){
						if(r.checkUser(userToJoin) || !r.privateChat){
							if(!currentChatRoom.equals("None")){
								currentChatRoomObj.deleteObserver(userObjToJoin);
							}
							r.addObserver(userObjToJoin);
							userObjToJoin.setChat(r.toString());
							sockWriter.println(ApprovedChars.signalingChar + "changeRoom " + toJoin);
							sockWriter.flush();
							sockWriter.println("Joining the following chatroom: " + toJoin);
							sockWriter.flush();
						}
						sjoinChat.close();
						return;
					}
				}
				sjoinChat.close();
				break;

			// Do nothing
			default:
				return;
			}
		}
	}

}
