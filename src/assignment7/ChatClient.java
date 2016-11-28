/* Chat Room ChatClient.java
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

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class ChatClient extends Application {
	private BufferedReader reader;
	private PrintWriter writer;
	private Pane mainPane;
	private TextArea currentChat;
	private TextArea sendText;
	private static Text loginErrorText;
	private Text onlineStatus;
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
	private static String signalingChar = "~"; // used to transmit commands to the server
	private ArrayList<Node> loginNodes = new ArrayList<Node>();
	private ArrayList<Node> chatNodes = new ArrayList<Node>();
	private ArrayList<Node> onlineNodes = new ArrayList<Node>();

	public void run() throws Exception {
		launch();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		
		primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		screenWidth = primaryScreenBounds.getWidth();
		screenHeight = primaryScreenBounds.getHeight();
		mainPane = new Pane();
		primaryStage.setScene(new Scene(mainPane, primaryScreenBounds.getWidth()*screenScale, primaryScreenBounds.getHeight()*screenScale));
		primaryStage.show();	
		initView();
		setUpNetworking();
	}

	private void initView() {
		
		btnWidth = (int) (screenWidth*.1*screenScale);
		btnHeight = (int) (screenHeight*.05*screenScale);
		canvasXPos = (int) (screenWidth*.5*screenScale);
		canvasYPos = (int) (screenHeight*.05*screenScale);
		canvasWidth = (int) (screenWidth*.4*screenScale);
		canvasHeight = (int) (screenHeight*.4*screenScale);

		loginDisplay();
		chatDisplay();
		
		
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
		
		TextArea usernameField = new TextArea();
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
		usernameField.setWrapText(false);
		usernameField.setPrefSize(screenWidth*.2*screenScale, 1);
		usernameField.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.05*screenScale);
		
		TextArea passwordField = new TextArea();
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
		passwordField.setWrapText(false);
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
				writer.println(signalingChar + "login " + usernameField.getText() + " " + passwordField.getText());
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
				writer.println(signalingChar + "register " + usernameField.getText() + " " + passwordField.getText());
				writer.flush();
				errorText.setVisible(false);
			}
		});
		
		//TODO: make this series of adds better
		loginNodes.add(username);
		loginNodes.add(password);
		loginNodes.add(usernameField);
		loginNodes.add(passwordField);
		loginNodes.add(loginBtn);
		loginNodes.add(registerBtn);
		loginNodes.add(errorText);
		mainPane.getChildren().addAll(username, password, usernameField, passwordField, loginBtn, registerBtn, errorText);		
	}
	
	private void chatDisplay(){
		currentChat = new TextArea();
		currentChat.setWrapText(true);
		currentChat.setEditable(false);
		currentChat.setMaxWidth(screenWidth*.4*screenScale);
		currentChat.relocate(canvasXPos, canvasYPos);
		currentChat.setPrefSize(canvasWidth, canvasHeight);
		
		sendText = new TextArea();
		sendText.setEditable(true);	
		sendText.relocate(canvasXPos + btnWidth*1.1, canvasYPos + canvasHeight + 25);
		sendText.setPrefSize(canvasWidth - btnWidth*1.1, 10);
		sendText.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
        	if(change.getControlNewText().contains(signalingChar)){ 
        		return null;
        	}
        	return change;
		}));
		sendText.setOnKeyPressed(new EventHandler<KeyEvent>(){
			@Override
		    public void handle(KeyEvent keyEvent) {
		        if (keyEvent.getCode() == KeyCode.ENTER)  {
		        	if(sendText.getText().trim().length() > 0){
						writer.println(sendText.getText().trim());
						writer.flush();
		        	}
					sendText.clear();
		        }
		    }
		});	
		
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
		
		//TODO: improve adding
		chatNodes.add(currentChat);
		chatNodes.add(sendText);
		chatNodes.add(sendButton);
		chatNodes.add(onlineStatus);
		chatNodes.add(logoutBtn);
		mainPane.getChildren().addAll(currentChat, sendText, sendButton, onlineStatus, logoutBtn);
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
		writer.println(signalingChar + "logout " + username);
		writer.flush();
		onlineStatus.setText("");
		for(Node n: loginNodes){
			n.setVisible(true);
		}
		for(Node n:chatNodes){
			n.setVisible(false);
		}	
	}
	
	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 4242);
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
					if(message.length() > 0 && message.charAt(0) == signalingChar.charAt(0)){
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
				loginErrorText.setText("Username enter not associate with any user");
				loginErrorText.setVisible(true);
				break;
				
			case "login":
				loginExecute(message.substring(message.indexOf(' ')+1));
				break;
				
			default:
				break;
		}
		
	}


}
