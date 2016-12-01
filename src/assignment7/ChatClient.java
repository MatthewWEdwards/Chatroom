/* Chat Room ChatClient.java
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

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class ChatClient extends Application {
	private BufferedReader reader;
	private PrintWriter writer;
	
	//JavaFX variables
	private Pane mainPane;
	private TextArea currentChat;
	private TextField sendText;
	private static Text loginErrorText;
	private Text onlineStatus;
	private Text currentChatRoom;
	private Rectangle2D primaryScreenBounds;
	private double screenScale = .5;
	private int canvasXPos;
	private int canvasYPos;
	private int canvasWidth;
	private int canvasHeight;
	private double screenWidth;
	private double screenHeight;
	private int btnWidth;
	private int btnHeight;
	private static final int maxUsernameLength = 15;
	private static final int maxPasswordLength = 15;
	private ArrayList<Node> loginNodes = new ArrayList<Node>();
	private ArrayList<Node> chatNodes = new ArrayList<Node>();
	private ArrayList<Node> networkNodes = new ArrayList<Node>();
	private ArrayList<Node> overallNodes = new ArrayList<Node>();
	private ListView<String> usersList;
	private ListView<String> chatRooms;
	private ObservableList<String> users = FXCollections.observableArrayList();
	private ObservableList<String> rooms = FXCollections.observableArrayList();
	private ObservableList<String> requestsWaiting = FXCollections.observableArrayList();
	private String PMDefault = "";

	public void run() throws Exception {
		launch();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		   primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

	            @Override
	            public void handle(WindowEvent event) {
	                Platform.runLater(new Runnable() {

	                    @Override
	                    public void run() {
	                    	if(onlineStatus.visibleProperty().get()){
	                    		logoutExecute(onlineStatus.getText().substring("Logged in as: ".length()));
	                    	}                   
	                    }
	                });
	            }
	        });
		
		primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		screenWidth = primaryScreenBounds.getWidth();
		screenHeight = primaryScreenBounds.getHeight();
		mainPane = new Pane();
		primaryStage.setScene(new Scene(mainPane, primaryScreenBounds.getWidth()*screenScale, primaryScreenBounds.getHeight()*screenScale));
		primaryStage.show();	
		initView();
	}

	private void initView() {
		
		btnWidth = (int) (screenWidth*.1*screenScale);
		btnHeight = (int) (screenHeight*.05*screenScale);
		canvasXPos = (int) (screenWidth*.56*screenScale);
		canvasYPos = (int) (screenHeight*.05*screenScale);
		canvasWidth = (int) (screenWidth*.4*screenScale);
		canvasHeight = (int) (screenHeight*.25*screenScale);

		Text serverIP = new Text();
		serverIP.relocate(screenWidth*.05*screenScale, 0);
		serverIP.setVisible(false);
		overallNodes.add(serverIP);
		mainPane.getChildren().add(serverIP);
		
		networkDisplay();
		loginDisplay();
		chatDisplay();
		
		
	}

	private void networkDisplay(){
		Text promptIP = new Text("Enter server IP: ");
		promptIP.relocate(screenWidth*.05*screenScale, screenHeight*.05*screenScale);
		
		Text connectionError = new Text("Server not found");
		connectionError.relocate(screenWidth*.05*screenScale, screenHeight*.15*screenScale);
		connectionError.setFill(Color.RED);
		connectionError.setVisible(false);
		
		TextField promptIPField = new TextField();
		promptIPField.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
			String testString = change.getControlNewText();
			if(testString.length() > maxUsernameLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		promptIPField.setPrefSize(screenWidth*.2*screenScale, 1);
		promptIPField.relocate(screenWidth*.05*screenScale + promptIP.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.05*screenScale);
		promptIPField.setText("127.0.0.1");
		
		Button connectBtn = new Button();
		connectBtn.setPrefSize(btnWidth, btnHeight);
		connectBtn.relocate(screenWidth*.05*screenScale + promptIPField.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.25*screenScale);
		connectBtn.setText("Login");
		connectBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					setUpNetworking(promptIPField.getText().trim());
					for(Node n:loginNodes){
						n.setVisible(true);
					}
					for(Node n:networkNodes){
						n.setVisible(false);
					}
					Text serverIP = (Text) overallNodes.get(0);
					serverIP.setText("ServerIP: " + promptIPField.getText());
					serverIP.setVisible(true);
				} catch (Exception e) {
					connectionError.setVisible(true);
				}
			}
		});
		
		networkNodes.add(promptIP);
		networkNodes.add(promptIPField);
		networkNodes.add(connectBtn);
		networkNodes.add(connectionError);
		mainPane.getChildren().addAll(promptIP, promptIPField, connectBtn, connectionError);
	}
	
	private void loginDisplay(){
		
		Text username = new Text("Username:");
		username.relocate(screenWidth*.05*screenScale, screenHeight*.05*screenScale);
		Text password = new Text("Password:");
		password.relocate(screenWidth*.05*screenScale, screenHeight*.15*screenScale);
		Text errorText = new Text();
		errorText.relocate(screenWidth*.05*screenScale, screenHeight*.35*screenScale);
		errorText.fillProperty().setValue(Color.RED);
		errorText.setVisible(false);
		loginErrorText = errorText;
		
		TextField usernameField = new TextField();
		usernameField.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
			String testString = change.getControlNewText();
			if(testString.length() > maxUsernameLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		usernameField.setPrefSize(screenWidth*.2*screenScale, 1);
		usernameField.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.05*screenScale);
		
		TextField passwordField = new TextField();
		passwordField.setTextFormatter(new TextFormatter<String>(change -> {// prevents strings that are too long and newlines
        	String testString = change.getControlNewText();
			if(testString.length() > maxPasswordLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		passwordField.setPrefSize(screenWidth*.2*screenScale, 1);
		passwordField.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.15*screenScale);
		
		Button loginBtn = new Button();
		loginBtn.setPrefSize(btnWidth, btnHeight);
		loginBtn.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.25*screenScale);
		loginBtn.setText("Login");
		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(usernameField.getText().length() < 1 || passwordField.getText().length() < 1){
					errorText.setText("Username and password textboxes must not be empty.");
					errorText.setVisible(true);
					return;
				}
				writer.println(ApprovedChars.signalingChar + "login " + usernameField.getText() + " " + passwordField.getText());
				writer.flush();
				errorText.setVisible(false);
			}
		});
		
		Button registerBtn = new Button();
		registerBtn.setPrefSize(btnWidth, btnHeight);
		registerBtn.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5 + (int) (screenWidth*.1*screenScale), screenHeight*.25*screenScale);
		registerBtn.setText("Register");
		registerBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(usernameField.getText().length() < 1 || passwordField.getText().length() < 1){
					errorText.setText("Username and password textboxes must not be empty.");
					errorText.setVisible(true);
					return;
					
				}
				writer.println(ApprovedChars.signalingChar + "register " + usernameField.getText() + " " + passwordField.getText());
				writer.flush();
				errorText.setVisible(false);
			}
		});
		

		loginNodes.add(username);
		loginNodes.add(password);
		loginNodes.add(usernameField);
		loginNodes.add(passwordField);
		loginNodes.add(loginBtn);
		loginNodes.add(registerBtn);
		loginNodes.add(errorText);
		for(Node n:loginNodes){
			n.setVisible(false);
		}
		mainPane.getChildren().addAll(username, password, usernameField, passwordField, loginBtn, registerBtn, errorText);		
	}
	
	private void chatDisplay(){
		currentChat = new TextArea();
		currentChat.setWrapText(true);
		currentChat.setEditable(false);
		currentChat.setMaxWidth(screenWidth*.4*screenScale);
		currentChat.relocate(canvasXPos, canvasYPos);
		currentChat.setPrefSize(canvasWidth, canvasHeight);
		
		sendText = new TextField();
		sendText.setEditable(true);	
		sendText.relocate(canvasXPos + btnWidth*1.1, canvasYPos + canvasHeight + 25);
		sendText.setPrefSize(canvasWidth - btnWidth*1.1, 10);
		sendText.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
        	if(change.getControlNewText().contains(ApprovedChars.signalingChar)){ 
        		return null;
        	}
        	return change;
		}));
		sendText.setOnKeyPressed(new EventHandler<KeyEvent>(){
			@Override
		    public void handle(KeyEvent keyEvent) {
		        if (keyEvent.getCode() == KeyCode.ENTER)  {
		        	if(sendText.getText().trim().length() > 0){
		        		String toSend = sendText.getText().trim();
		        		sendText.setText("");
						writer.println(toSend);
						writer.flush();
		        	}
					sendText.clear();
					sendText.setText(PMDefault);
					sendText.positionCaret(PMDefault.length() + 1);
		        }
		    }
		});	
		
		Text enterMessage = new Text("Enter Message:");
		enterMessage.relocate(canvasXPos + btnWidth*1.1, canvasYPos + canvasHeight + 5);
		
		Button sendButton = new Button("Send");
		sendButton.setPrefSize(btnWidth, btnHeight);
		sendButton.relocate(canvasXPos, canvasYPos + canvasHeight + 25);
		sendButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(sendText.getText().trim().length() > 0){
					writer.println(sendText.getText());
					writer.flush();
				}
					sendText.clear();
					sendText.setText(PMDefault);
					sendText.positionCaret(PMDefault.length() + 1);
			}
		});
		
		onlineStatus = new Text();
		onlineStatus.relocate(screenWidth*.05*screenScale, screenHeight*.05*screenScale);
		
		Button logoutBtn = new Button("Logout");
		logoutBtn.setPrefSize(btnWidth, btnHeight);
		logoutBtn.relocate(screenWidth*.05*screenScale, screenHeight*.15*screenScale);
		logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				logoutExecute(onlineStatus.getText().substring("Logged in as: ".length()));
			}
		});
		Text requestFriend = new Text("Request Friends:");
		requestFriend.relocate(screenWidth*.05*screenScale, screenHeight*.23*screenScale);
		TextField requestFriendField = new TextField();
		requestFriendField.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
			String testString = change.getControlNewText();
			if(testString.length() > maxUsernameLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		requestFriendField.setPrefSize(screenWidth*.2*screenScale, 1);
		requestFriendField.relocate(screenWidth*.05*screenScale + requestFriend.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.23*screenScale);
		
		Button requestFriendBtn = new Button("Send Request");
		requestFriendBtn.setPrefSize(btnWidth*2, btnHeight);
		requestFriendBtn.relocate(screenWidth*.05*screenScale, screenHeight*.42*screenScale);
		requestFriendBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				requestFriendExecute(requestFriendField.getText(), onlineStatus.getText().substring("Logged in as: ".length()));
			}
		});
		
		
		
		final ComboBox<String> friendRequestsWaiting = new ComboBox<>(requestsWaiting);
		friendRequestsWaiting.setEditable(true);
		friendRequestsWaiting.relocate(screenWidth*.05*screenScale, screenHeight*.59*screenScale);
		
		Button checkRequestsBtn = new Button("Check Friend Requests");
		checkRequestsBtn.setPrefSize(btnWidth*2, btnHeight);
		checkRequestsBtn.relocate(screenWidth*.05*screenScale, screenHeight*.52*screenScale);
		checkRequestsBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				checkRequestExecute(onlineStatus.getText().substring("Logged in as: ".length()), requestsWaiting, friendRequestsWaiting);
			}
		});
		
		chatRooms = new ListView<String>();
		chatRooms.setItems(rooms);
		ScrollPane chatRoomsPane = new ScrollPane();
		chatRoomsPane.setContent(chatRooms);
		chatRoomsPane.relocate(screenWidth*.71*screenScale, screenHeight*.52*screenScale);
		chatRoomsPane.setPrefSize(screenWidth*.25*screenScale, screenHeight*.20*screenScale);
		
		Text roomsList = new Text("Chat rooms available");
		roomsList.relocate(screenWidth*.71*screenScale, screenHeight*.48*screenScale);
		
		usersList = new ListView<String>();
		usersList.setItems(users);
		ScrollPane usersListPane = new ScrollPane();
		usersListPane.setContent(usersList);
		usersListPane.relocate(screenWidth*.71*screenScale, screenHeight*.80*screenScale);
		usersListPane.setPrefSize(screenWidth*.25*screenScale, screenHeight*.20*screenScale);
		
		Text userList = new Text("Users online");
		userList.relocate(screenWidth*.71*screenScale, screenHeight*.76*screenScale);
		
		
		TextField ChatRoomName = new TextField();
		ChatRoomName.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
			String testString = change.getControlNewText();
			if(testString.length() > maxUsernameLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		ChatRoomName.setPrefSize(screenWidth*.15*screenScale, 1);
		ChatRoomName.relocate(screenWidth*.36*screenScale, screenHeight*.52*screenScale);
		
		Text enterChatRoom = new Text("Enter Chatroom:");
		enterChatRoom.relocate(screenWidth*.36*screenScale, screenHeight*.48*screenScale);
		
		Button publicChatRoomBtn = new Button();
		publicChatRoomBtn.setText("Make public chat");
		publicChatRoomBtn.setPrefSize(btnWidth*1.5, btnHeight);
		publicChatRoomBtn.relocate(screenWidth*.36*screenScale, screenHeight*.62*screenScale);
		publicChatRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(ChatRoomName.getText().length() > 1){
				String username = onlineStatus.getText().substring("Logged in as: ".length());
				writer.println(ApprovedChars.signalingChar + "makeChatRoom " + ChatRoomName.getText() + " " + username + " false");
				writer.flush();
				}
			}
		});
		
		Button privateChatRoomBtn = new Button();
		privateChatRoomBtn.setText("Make private chat");
		privateChatRoomBtn.setPrefSize(1.5*btnWidth, btnHeight);
		privateChatRoomBtn.relocate(screenWidth*.36*screenScale, screenHeight*.68*screenScale);
		privateChatRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(ChatRoomName.getText().length() > 1){
					String username = onlineStatus.getText().substring("Logged in as: ".length());
					writer.println(ApprovedChars.signalingChar + "makeChatRoom " + ChatRoomName.getText() + " " + username + " true");
					writer.flush();
				}
			}
		});
		
		TextField userPermission = new TextField();
		userPermission.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
			String testString = change.getControlNewText();
			if(testString.length() > maxUsernameLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		userPermission.setPrefSize(screenWidth*.15*screenScale, 1);
		userPermission.relocate(screenWidth*.52*screenScale, screenHeight*.52*screenScale);
		
		Text enterUser = new Text("Enter User:");
		enterUser.relocate(screenWidth*.52*screenScale, screenHeight*.48*screenScale);
		
		Button giveUserPermissions = new Button();
		giveUserPermissions.setText("Add user to room");
		giveUserPermissions.setPrefSize(1.5*btnWidth, btnHeight);
		giveUserPermissions.relocate(screenWidth*.52*screenScale, screenHeight*.62*screenScale);
		giveUserPermissions.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(userPermission.getText().length() > 1){
					String username = onlineStatus.getText().substring("Logged in as: ".length());
					writer.println(ApprovedChars.signalingChar + "giveUserPermissions " + ChatRoomName.getText() + " " + username + " " + userPermission.getText());
					writer.flush();
				}
			}
		});
		
		currentChatRoom = new Text("Current Chatroom: None");
		currentChatRoom.relocate(screenWidth*.56*screenScale, 0);

		
		Button joinChatBtn = new Button();
		joinChatBtn.setText("Join Chat");
		joinChatBtn.setPrefSize(1.5*btnWidth, btnHeight);
		joinChatBtn.relocate(screenWidth*.36*screenScale, screenHeight*.74*screenScale);
		joinChatBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(ChatRoomName.getText().length() > 1){
					String username = onlineStatus.getText().substring("Logged in as: ".length());
					writer.println(ApprovedChars.signalingChar + "joinChat " + ChatRoomName.getText() + " "
					+ username + " " + currentChatRoom.getText().substring("Current Chatroom:".length()));
					writer.flush();
				}
			}
		});
		
		Button privateChatBtn = new Button();
		privateChatBtn.setText("Default PM user");
		privateChatBtn.setPrefSize(1.5*btnWidth, btnHeight);
		privateChatBtn.relocate(screenWidth*.52*screenScale, screenHeight*.68*screenScale);
		privateChatBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(userPermission.getText().length() > 0){
					PMDefault = "#" + userPermission.getText() + " ";
					sendText.clear();
					sendText.setText(PMDefault);
					sendText.positionCaret(PMDefault.length() + 1);
				}
			}
		});
		
		TextArea PotentialFriendName = new TextArea();
		PotentialFriendName.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
			String testString = change.getControlNewText();
			if(testString.length() > maxUsernameLength){
        		return null;
        	}else{
        		for(int i = 0; i < testString.length(); i++){
        			if(!ApprovedChars.approvedCharSet.contains(testString.charAt(i))){
        				return null;
        			}
        		}
        		return change;
        	}
		}));
		PotentialFriendName.setWrapText(false);
		PotentialFriendName.setPrefSize(screenWidth*.25*screenScale, .4);
		PotentialFriendName.relocate(screenWidth*.05*screenScale, screenHeight*.72*screenScale);
		
		Text responseToRequest = new Text("Respond to Friend Requests:");
		responseToRequest.relocate(screenWidth*.05*screenScale, screenHeight*.67*screenScale);
		 
		Button acceptFriendBtn = new Button();
		acceptFriendBtn.setText("Accept");
		acceptFriendBtn.setPrefSize(btnWidth, btnHeight);
		acceptFriendBtn.relocate(screenWidth*.05*screenScale, screenHeight*.85*screenScale);
		acceptFriendBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(PotentialFriendName.getText().length() > 1){ //create text area to enter about friend
					String username = onlineStatus.getText().substring("Logged in as: ".length());
					writer.println(ApprovedChars.signalingChar + "acceptFriend " + PotentialFriendName.getText() + " "
					+ username );
					writer.flush();
					PotentialFriendName.clear();
				}
			}
		});
		
		Button declineFriendBtn = new Button();
		declineFriendBtn.setText("Decline");
		declineFriendBtn.setPrefSize(btnWidth, btnHeight);
		declineFriendBtn.relocate(screenWidth*.2*screenScale, screenHeight*.85*screenScale);
		declineFriendBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(PotentialFriendName.getText().length() > 1){ //create text area to enter about friend
					String username = onlineStatus.getText().substring("Logged in as: ".length());
					writer.println(ApprovedChars.signalingChar + "declineFriend " + PotentialFriendName.getText() + " "
					+ username );
					writer.flush();
					PotentialFriendName.clear();
				}
			}
		});
		
		chatNodes.add(currentChat);
		chatNodes.add(sendText);
		chatNodes.add(sendButton);
		chatNodes.add(onlineStatus);
		chatNodes.add(logoutBtn);
		chatNodes.add(requestFriendField);
		chatNodes.add(requestFriend);
		chatNodes.add(requestFriendBtn);
		chatNodes.add(friendRequestsWaiting);
		chatNodes.add(checkRequestsBtn);
		chatNodes.add(chatRoomsPane);
		chatNodes.add(usersListPane);
		chatNodes.add(ChatRoomName);
		chatNodes.add(publicChatRoomBtn);
		chatNodes.add(privateChatRoomBtn);
		chatNodes.add(userPermission);
		chatNodes.add(giveUserPermissions);
		chatNodes.add(joinChatBtn);
		chatNodes.add(currentChatRoom);
		chatNodes.add(roomsList);
		chatNodes.add(userList);
		chatNodes.add(enterUser);
		chatNodes.add(enterChatRoom);
		chatNodes.add(enterMessage);
		chatNodes.add(privateChatBtn);
		
		mainPane.getChildren().addAll(currentChat, sendText, sendButton, onlineStatus, logoutBtn,
				requestFriendField, requestFriend, requestFriendBtn, friendRequestsWaiting, checkRequestsBtn, 
				chatRoomsPane, usersListPane, ChatRoomName, publicChatRoomBtn, privateChatRoomBtn, userPermission, 
				giveUserPermissions, joinChatBtn, currentChatRoom, roomsList, userList, enterUser,enterChatRoom,
				enterMessage, privateChatBtn);
		for(Node n:chatNodes){
			n.setVisible(false);
		}
	}
	
	private void loginExecute(String username){
		onlineStatus.setText("Logged in as: " + username);
		
		for(Node n: loginNodes){
			n.setVisible(false);
		}
		for(Node n:chatNodes){
			n.setVisible(true);
		}	
		
	}

	private void logoutExecute(String username){
		writer.println(ApprovedChars.signalingChar + "logout " + username);
		writer.flush();
		onlineStatus.setText("");
		for(Node n: loginNodes){
			n.setVisible(true);
		}
		for(Node n:chatNodes){
			n.setVisible(false);
		}	
	}
	
	private void requestFriendExecute(String friendName, String thisUser){
		writer.println(ApprovedChars.signalingChar + "request " + friendName + " " + thisUser );
		writer.flush();

		
	}
	
	private void checkRequestExecute(String thisUser, ObservableList<String> requestsWaiting, ComboBox<String> friendRequestsWaiting){
		writer.println(ApprovedChars.signalingChar + "checkRequest " + thisUser );
		writer.flush();
		friendRequestsWaiting.setItems(requestsWaiting);
		
	}
	
	private void setUpNetworking(String IP) throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket(IP, 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	public static void main(String[] args) {
		try {
			new ChatClient().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				
				while ((message = reader.readLine()) != null) {
					if(message.length() > 0 && message.charAt(0) == ApprovedChars.signalingChar.charAt(0)){
						serverCommands(message);
					}else{
						currentChat.appendText(message + "\n");
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void serverCommands(String message){
		switch(message.substring(1, message.indexOf(' '))){
			case "registerError":
				loginErrorText.setText("Username taken");
				loginErrorText.setVisible(true);
				break;
				
			case "passwordMismatch":
				loginErrorText.setText("Password incorrect");
				loginErrorText.setVisible(true);
				break;
				
			case "alreadyOnline":
				loginErrorText.setText("User already online");
				loginErrorText.setVisible(true);
				break;
				
			case "noUser":
				loginErrorText.setText("Username entered not associated with any user");
				loginErrorText.setVisible(true);
				break;
				
			case "login":
				loginExecute(message.substring(message.indexOf(' ')+1));
				break;
				
			case "sameUser":
				loginErrorText.setText("Cannot friend request yourself");
				loginErrorText.setVisible(true);
				break;
				
			
			case "alreadyFriends":
				loginErrorText.setText("Already friends with this user");
				loginErrorText.setVisible(true);
				break;
				
			case "requestSent":
				loginErrorText.setText("Friend request sent");
				loginErrorText.setVisible(true);
				break;
			case "alreadyRequested":
				loginErrorText.setText("Already requested this user");
				loginErrorText.setVisible(true);
				break;
			case "doubleRequest":
				loginErrorText.setText("This user has request you, check requests");
				loginErrorText.setVisible(true);
				break;
			case "requestReturn":
				String requests = message.substring(message.indexOf('[')+1, message.indexOf(']'));
				requestsWaiting.clear();
				int i = 0;
				
				boolean q = requests.contains(",");
				if(q == true){
				requestsWaiting.add(requests.substring(0, requests.indexOf(',')));
				i = requests.indexOf(',') + 2;
				while( requests.indexOf(',') != requests.lastIndexOf(',')){
				requests = requests.substring(i);
				requestsWaiting.add(requests.substring(0, requests.indexOf(',')));
				i = requests.indexOf(',') + 2;
				}
				}
				requestsWaiting.add(requests.substring(i));
				break;
			case "updateUsers":
				Platform.runLater(new Runnable() {
				    @Override
			    public void run() {
			    	users.clear();				   
					String newUsers = message.substring(message.indexOf(' ')+1);
					Scanner s = new Scanner(newUsers);
					while(s.hasNext()){
						users.add(s.next());
					}
					s.close();
				    }
				});
				break;
				
			case "updateRooms":
				Platform.runLater(new Runnable() {
				    @Override
			    public void run() {
			    	rooms.clear();				   
					String newRooms = message.substring(message.indexOf(' ')+1);
					Scanner s = new Scanner(newRooms);
					while(s.hasNext()){
						rooms.add(s.next());
					}
					s.close();
				    }
				});
				break;
				
			case "changeRoom":
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				    	String changeRoom = message.substring(message.indexOf(' ')+1);
						Scanner s = new Scanner(changeRoom);
						currentChatRoom.setText("Current Chatroom: " + s.next());
				    }
					});
				
				break;
				
			default:
				break;
		}
		
	}
	



}
