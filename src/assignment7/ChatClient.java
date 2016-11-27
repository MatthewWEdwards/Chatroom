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
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class ChatClient extends Application {
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
	

	public void run() throws Exception {
		launch();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		initView();
		setUpNetworking();
		
		
		primaryStage.setScene(new Scene(mainPane, primaryScreenBounds.getWidth()*screenScale, primaryScreenBounds.getHeight()*screenScale));
		primaryStage.show();
	}

	private void initView() {
		primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		int btnWidth = (int) (primaryScreenBounds.getWidth()*.05*screenScale);
		int btnHeight = (int) (primaryScreenBounds.getHeight()*.05*screenScale);
		canvasXPos = (int) (primaryScreenBounds.getWidth()*.6*screenScale);
		canvasYPos = (int) (primaryScreenBounds.getHeight()*.6*screenScale*0); // note the 0
		canvasWidth = (int) (primaryScreenBounds.getWidth()*.4*screenScale);
		canvasHeight = (int) (primaryScreenBounds.getHeight()*.4*screenScale);
		
		mainPane = new Pane();
		currentChat = new TextArea();
		currentChat.setWrapText(true);
		currentChat.setEditable(false);
		currentChat.setMaxWidth(primaryScreenBounds.getWidth()*.4*screenScale);
		currentChat.relocate(canvasXPos, canvasYPos);
		currentChat.setPrefSize(canvasWidth, canvasHeight);
		mainPane.getChildren().add(currentChat);
		
		sendText = new TextArea();
		sendText.setEditable(true);
		sendText.relocate(canvasXPos + btnWidth*1.1, canvasYPos + canvasHeight + 25);
		sendText.setPrefSize(canvasWidth - btnWidth, 10);
		mainPane.getChildren().add(sendText);
		
		Button sendButton = new Button("Send");
		sendButton.resize(btnWidth, btnHeight);
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

/*	class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			writer.println(outgoing.getText());
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}
	*/

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
