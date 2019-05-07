package ClientSide;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ServerSide.Server;
import ServerSide.Database;


import java.io.*;
import java.net.Socket;
import java.util.*;


public class Client extends Application {

    public static final String newLine = System.lineSeparator();
   

    // GUI
    private ArrayList<privateDM> privateAndGroup;							// 
    private boolean signIn = true;
    private TabPane tabPane;
    private TextArea sentMessages;
    private TextField messageBox;
    private Button sendBtn;
    private List<String> usersOnNetwork;
    //

    ObjectOutputStream objectWriter;
    ObjectInputStream objectReader;
    Scanner input;
    Socket sock;

    String clientID;

    public GridPane setUpGridPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.addRow(100);
        grid.addColumn(100);
        grid.setVgap(1);
        return grid;
    }

    public void setUpSentMessages() {
        sentMessages = new TextArea();
        sentMessages.setEditable(false);
        sentMessages.setWrapText(true);
        GridPane.setConstraints(sentMessages,0,0);
        sentMessages.setMinWidth(450);
        sentMessages.setMinHeight(400);
    }

    public void setUpMessageBox() {
        messageBox = new TextField();
        GridPane.setConstraints(messageBox, 0, 6);

        messageBox.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                String keyPressed = event.getCharacter();
                String message = messageBox.getText().replaceAll(newLine, "");
                if ((keyPressed.contains("\r")|| keyPressed.contains("\n") ||
                        keyPressed.contains(newLine)) && !message.isEmpty()) {
                    try {

                        // TODO: remove after testing
                        DataPacket data = new DataPacket("public", new String[]{clientID}, message);
                        objectWriter.writeObject(data);
                        objectWriter.flush();
                        objectWriter.reset();
                    } catch (IOException e) {
                    	//e.printStackTrace();
                    	}
                    messageBox.clear();
                }
            }
        });
    }

    public void setUpSendButton() {
        sendBtn = new Button("send");
        sendBtn.setMinWidth(50);
        GridPane.setConstraints(sendBtn, 1, 10);
        sendBtn.setTranslateX(-50);

        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = messageBox.getText().replaceAll(newLine, "");
                if (!message.isEmpty()) {
                    try {
                        DataPacket data = new DataPacket("public", new String[]{clientID}, message);
                        objectWriter.writeObject(data);
                        objectWriter.flush();
                        objectWriter.reset();
                        messageBox.clear();
                    } catch (IOException e) {e.printStackTrace();}
                }
            }
        });

    }
    
//    private GridPane setUpDMGroupMsg() {
//    	GridPane 
//    }
    private GridPane setUpDMTab() {
    	GridPane DMGrid = setUpGridPane();
    	VBox LeftVBox = new VBox();
    	VBox recipientVBox = new VBox();
    	recipientVBox.setPadding(new Insets(10, 10, 10, 10));
    	recipientVBox.setSpacing(10);
    	VBox MsgVBox = new VBox();
    	LeftVBox.getChildren().addAll(recipientVBox, MsgVBox);
    	
    	
    	for (int i = 0; i < 10; i++) {
    		CheckBox cb3 = new CheckBox();
        	cb3.setText(clientID + "i");
        	cb3.setIndeterminate(false);
        	recipientVBox.getChildren().addAll(cb3);
    	}
//    	Button messageBtn1 = new Button();
//    	messageBtn1.setText("Message1");
//    	recipientVBox.getChildren().addAll(messageBtn1);
    	
    	
//    	HBox hbox = new HBox();
    	

//    	DMGrid.getChildren().addAll(vbox, cb);
    		 
    	Button messageBtn = new Button();
    	messageBtn.setText("Message");
    	GridPane.setConstraints(messageBtn, 0, 6);
    	recipientVBox.getChildren().addAll(messageBtn);
    	messageBtn.setOnAction(new EventHandler<ActionEvent>() {
    		@Override
    	    public void handle(ActionEvent event) {
    			//Tab newDMTab = privateDM(String clientID, ObjectOutputStream objectWriter, Integer uniqueNum, ArrayList<String> recipients);
    	    }
    	});

    	
    	ScrollPane scrollPane = new ScrollPane();
      	scrollPane.setMaxHeight(200);
      	scrollPane.setMinWidth(200);
      	scrollPane.setContent(recipientVBox);
      	
      	
      	LeftVBox.getChildren().addAll(scrollPane);
    		 
    		 
    	DMGrid.getChildren().addAll(LeftVBox);
    		 
    		 
    		 
    	
    	return DMGrid;
    }

    private GridPane setUpPublicTab() {
    	GridPane grid = setUpGridPane();
    	setUpSentMessages();
        setUpMessageBox();
        setUpSendButton();
        grid.getChildren().addAll(sentMessages, messageBox, sendBtn);
        return grid;
    	
    }
    
    private GridPane setUpSignInTab(Stage primaryStage) {
    	GridPane signInGrid = setUpGridPane();
    	TextField username = new TextField();
    	  GridPane.setConstraints(username, 0, 6); 

          username.setOnKeyTyped(new EventHandler<KeyEvent>() {
              @Override
              public void handle(KeyEvent event) {
                  String keyPressed = event.getCharacter();
                  String message = username.getText().replaceAll(newLine, "");
                  if ((keyPressed.contains("\r")|| keyPressed.contains("\n") ||
                          keyPressed.contains(newLine)) && !message.isEmpty()) {
                     clientID = message;
                     System.out.println(clientID);
                     signIn = false;
                     
                     try {
                 		DataPacket data = new DataPacket("signIn", new String[]{clientID}, "");
                 		objectWriter.writeObject(data);
                 		Thread.sleep(1000);
                 		setUpMainTabs(primaryStage);
                 		objectWriter.flush();
                 		objectWriter.reset();
                 		
                 	} catch (Exception e) {      	}
                     
                  }
              }
          });
          signInGrid.getChildren().addAll(username);
    	return signInGrid;
    }
    
    private void showTabPane(TabPane tabPane, Stage primaryStage) {
    	Scene scene = new Scene(tabPane, 470, 480);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Chat Room");
		primaryStage.show();
    }
    
    private void setUpMainTabs(Stage primaryStage) {
    	TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		Tab publicTab = new Tab();
		publicTab.setText("Public");
		GridPane publicGrid = setUpPublicTab();
		publicTab.setContent(publicGrid);
		tabPane.getTabs().add(publicTab);
	
		
		Tab dmTab = new Tab();
		dmTab.setText("Direct/Group Messages");
		GridPane dmGrid = setUpDMTab();
		dmTab.setContent(dmGrid);
		tabPane.getTabs().add(dmTab);
		
		showTabPane(tabPane, primaryStage);

		
    }
    
    public void initiateGui(Stage primaryStage) {
    	
        
    	tabPane = new TabPane();
    	Tab signInTab = new Tab();
		signInTab.setText("Sign-In");
		GridPane signInGrid = setUpSignInTab(primaryStage);
		signInTab.setContent(signInGrid);
		tabPane.getTabs().add(signInTab);
	
    

		showTabPane(tabPane, primaryStage);
    }

    public void setUpNetwork() throws IOException {
        input = new Scanner(System.in);
        sock = new Socket("localhost", 5001);

        if (sock.isConnected()) {
            System.out.println("connection established");
        }
        objectReader = new ObjectInputStream(sock.getInputStream());
        objectWriter = new ObjectOutputStream(sock.getOutputStream());

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    class IncomingReader implements Runnable {
        public void run() {
            Object m;
            try {
                // wait until Gui has initiated to receive messages
                while (sentMessages == null) {
                    Thread.sleep(1000);
                }
                while ((m = objectReader.readObject()) != null) {

                    DataPacket message = (DataPacket) m;


                    if (message.type.contentEquals("usersOnNetwork/")) {
                    	
                    }
                    
                    sentMessages.appendText(message + newLine);
                }
                sock.close();
            } catch (IOException e) {
            	//e.printStackTrace();
            	}
            catch (ClassNotFoundException e) {e.printStackTrace();}
            catch (InterruptedException e) {e.printStackTrace();}
        }
    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Client c1 = new Client();
        System.out.println("starting up client");
        
        c1.setUpNetwork();
        c1.initiateGui(primaryStage);
    }
}
