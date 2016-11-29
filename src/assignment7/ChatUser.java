/* Chat Room ChatUser.java
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

import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;


public class ChatUser implements Observer{
	private String username;
	private String password; //TODO: salted hash?
	private boolean online;
	private PrintWriter userWriter;
	private int port;
	
	public ChatUser(String username, String password){
		this.username = username;
		this.password = password;
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
		// TODO Auto-generated method stub
		
	}
	
}