/* Chat Room ChatUser.java
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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class ChatUser implements Observer{
	private String username;
	private String password; 
	private boolean online;
	private PrintWriter userWriter;
	private int port;
	private String currentChat;
	private ArrayList<String> friends;
	private ArrayList<String> requests;
	
	public ChatUser(String username, String password){
		this.username = username;
		this.password = password;
		friends = new ArrayList<String>();
		requests = new ArrayList<String>();
	}
	
	public String getChat(){
		return currentChat;
	}
	
	public void setChat(String currentChat){
		this.currentChat = currentChat;
	}
	
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public boolean getOnlineStatus(){
		return online;
	}
	
	public void setOnlineStatus(boolean set){
		online = set;
	}
	
	public PrintWriter getWriter(){
		return userWriter;
	}
	
	public void setWriter(PrintWriter userWriter){
		this.userWriter = userWriter;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public ArrayList<String> getFriends(){
		return this.friends;
	}
	
	public ArrayList<String> getRequests(){
		return this.requests;
	}
	
	public void addToFriends(String newFriend){
		friends.add(newFriend);
		requests.remove(newFriend);
	}
	
	public void addToRequests(String newRequest){
		requests.add(newRequest);
	}
	
	@Override
	public boolean equals(Object u){
		if(!(u instanceof ChatUser)){
			return false;
		}
		ChatUser testUser = (ChatUser) u;
		if(testUser.username.equals(this.username)){
			return true;
		}
		return false;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof ChatRoom){
			if(arg1 instanceof String){
				userWriter.println(arg1);
				userWriter.flush();
			}
		}
		
	}
	
	public void getPrivateMessage(String message){
		Scanner privateMessageScanner = new Scanner(message);
		String lineOne = privateMessageScanner.next();
		if(privateMessageScanner.hasNext()){
			String lineTwo = privateMessageScanner.nextLine();
			userWriter.println(lineOne + lineTwo);
			userWriter.flush();
		}else{
			userWriter.println(message);
			userWriter.flush();
		}
		privateMessageScanner.close();
	}
	
	@Override
	public String toString(){
		return username;
	}
	
}
