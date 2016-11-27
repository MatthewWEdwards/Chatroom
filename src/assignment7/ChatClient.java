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
import javax.swing.*;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class ChatClient extends Application {
	private Socket clientSocket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Pane mainPane;
	private TextArea currentChat;
	private TextArea sendText;
	private Rectangle2D primaryScreenBounds;
	private double screenScale = .5;
	private int canvasXPos;
	private int canvasYPos;
	private int canvasWidth;
	private int canvasHeight;
	private double screenWidth;
	private double screenHeight;
	private static final int maxUsernameLength = 15;
	private static final int maxPasswordLength = 15;
	private static final String signalingChar = "~"; // used to transmit commands to the server

	public void run() throws Exception {
		launch();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		setUpNetworking();
		
		primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		screenWidth = primaryScreenBounds.getWidth();
		screenHeight = primaryScreenBounds.getHeight();
		mainPane = new Pane();
		primaryStage.setScene(new Scene(mainPane, primaryScreenBounds.getWidth()*screenScale, primaryScreenBounds.getHeight()*screenScale));
		primaryStage.show();	
		initView();
	}

	private void initView() {
		
		int btnWidth = (int) (screenWidth*.1*screenScale);
		int btnHeight = (int) (screenHeight*.05*screenScale);
		canvasXPos = (int) (screenWidth*.5*screenScale);
		canvasYPos = (int) (screenHeight*.05*screenScale);
		canvasWidth = (int) (screenWidth*.4*screenScale);
		canvasHeight = (int) (screenHeight*.4*screenScale);

		login();
		
		currentChat = new TextArea();
		currentChat.setWrapText(true);
		currentChat.setEditable(false);
		currentChat.setMaxWidth(screenWidth*.4*screenScale);
		currentChat.relocate(canvasXPos, canvasYPos);
		currentChat.setPrefSize(canvasWidth, canvasHeight);
		mainPane.getChildren().add(currentChat);
		
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
		mainPane.getChildren().add(sendText);
		
		Button sendButton = new Button("Send");
		sendButton.setPrefSize(btnWidth, btnHeight);
		sendButton.relocate(canvasXPos, canvasYPos + canvasHeight + 25);
		sendButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writer.println(sendText.getText());
				writer.flush();
				sendText.clear();
			}
		});
		mainPane.getChildren().add(sendButton);
	}

	private void login(){
		
		Text username = new Text("Username:");
		username.relocate(screenWidth*.05*screenScale, screenHeight*.05*screenScale);
		Text password = new Text("Password:");
		password.relocate(screenWidth*.05*screenScale, screenHeight*.15*screenScale);
		
		TextArea usernameField = new TextArea();
		usernameField.setTextFormatter(new TextFormatter<String>(change -> { // prevents strings that are too long and newlines
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
		loginBtn.setPrefSize((int) (screenWidth*.1*screenScale), (int) (screenHeight*.05*screenScale));
		loginBtn.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5, screenHeight*.25*screenScale);
		loginBtn.setText("Login");
		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(usernameField.getText().length() < 1 || passwordField.getText().length() < 1){
					return;
					//TODO: no text protocol
				}
				writer.println(signalingChar + "login " + Integer.valueOf((clientSocket.getLocalPort())).toString() + "~" + usernameField.getText() + " " + passwordField.getText());
				writer.flush();
			}
		});
		
		Button registerBtn = new Button();
		registerBtn.setPrefSize((int) (screenWidth*.1*screenScale), (int) (screenHeight*.05*screenScale));
		registerBtn.relocate(screenWidth*.05*screenScale + username.boundsInLocalProperty().get().getWidth() + 5 + (int) (screenWidth*.1*screenScale), screenHeight*.25*screenScale);
		registerBtn.setText("Register");
		registerBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(usernameField.getText().length() < 1 || passwordField.getText().length() < 1){
					return;
					//TODO: no text protocol
				}
				writer.println(signalingChar + "register " + usernameField.getText() + " " + passwordField.getText());
				writer.flush();
			}
		});
		
		mainPane.getChildren().addAll(username, password, usernameField, passwordField, loginBtn, registerBtn);		
	}
	
	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 4242);
		clientSocket = sock;
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
					currentChat.appendText(message + "\n");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}


}
