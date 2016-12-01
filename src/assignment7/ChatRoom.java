/* Chat Room ChatRoom.java
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

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class ChatRoom extends Observable{
	public String name;
	public boolean privateChat;
	private Set<String> approvedUsers = new HashSet<String>();
	public String owner;
	
	public ChatRoom(String name, String owner, boolean privateChat){
		this.name = name;
		this.privateChat = privateChat;
		this.owner = owner;
	}
	
	public void getMessage(String message){
		this.setChanged();
		notifyObservers(message);
	}
	
	public void addUser(String u){
		approvedUsers.add(u);
	}
	
	public void removeUser(String u){
		approvedUsers.remove(u);
	}
	
	public boolean checkUser(String u){
		if(approvedUsers.contains(u)){
			return true;
		}
		return false;
	}
	
	@Override
	public void addObserver(Observer u){
		if(!(u instanceof ChatUser)){
			return;
		}
		ChatUser userToAdd = (ChatUser) u;
		if(!privateChat){
			super.addObserver(userToAdd);
		}else{
			if(approvedUsers.contains(userToAdd.toString())){
				super.addObserver(userToAdd);
			}
		}
	}
	
	@Override
	public boolean equals(Object r){
		if(!(r instanceof ChatRoom)){
			return false;
		}
		ChatRoom testRoom = (ChatRoom) r;
		if(testRoom.name.equals(this.name)){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return name;
	}

}
