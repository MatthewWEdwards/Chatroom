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

public class ChatUser{
	private String username;
	private String password; //TODO: salted hash?
	private boolean online;
	private PrintWriter outputStream;
	
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
	
	public PrintWriter getOutputStream(){
		return outputStream;
	}
	
	public void setOutputStream(PrintWriter outputStream){
		this.outputStream = outputStream;
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
	
}