package ClientSide;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class Client extends Application {

    public static final String newLine = System.lineSeparator();

    public static HashMap<Set<String>, Conversation> conversations = new HashMap<>();

    private ObservableList<String> usernamesOnline;

    // GUI
   
    private boolean signIn = true;
    private TabPane tabPane;
    private TextArea sentMessages;
    private TextField messageBox;
    private Button sendBtn;
    private VBox conversationList;
    private HBox directMessageWindow;
    private ListView<String> usersOnline;
    private TextField membersToAdd;
    private TextField IPAddressBox;
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
        sentMessages.setMinWidth(680);
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
                    List<String> clientUsername = new ArrayList<>();
                    clientUsername.add(clientID);
                    try {
                        // TODO: remove after testing
                        DataPacket data = new DataPacket("public", clientUsername, message);
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
                    List<String> clientUsername = new ArrayList<>();
                    clientUsername.add(clientID);
                    try {
                        DataPacket data = new DataPacket("public",clientUsername, message);
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
    private HBox setUpDMTab() {
        Button makeNewConvo = new Button("+");
        membersToAdd = new TextField();

        usersOnline = new ListView<>(usernamesOnline);
        usersOnline.setMaxWidth(100);
        usersOnline.setMinWidth(100);
        usersOnline.setOnMouseClicked(event -> membersToAdd.appendText(
                usersOnline.getSelectionModel().getSelectedItem() + ","));

        conversationList = new VBox(makeNewConvo, membersToAdd);
        ScrollPane scrollPane = new ScrollPane(conversationList);
        scrollPane.setVmax(200);
        scrollPane.setMinWidth(202);

        directMessageWindow = new HBox(usersOnline, scrollPane);

        makeNewConvo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!membersToAdd.getText().equals("")) {
                    String usernames = membersToAdd.getText();
                    membersToAdd.clear();
                    List<String> users = new ArrayList<>();
                    users.add(clientID);
                    users.addAll(new ArrayList<>(Arrays.asList(usernames.split(","))));
                    System.out.println(users);
                    // TODO: server needs to send this
                    /*ConversationLabel c1 = new ConversationLabel(directMessageWindow, objectWriter, users);
                    conversations.put(c1.convo.convoMembers, c1.convo);
                    conversationList.getChildren().add(c1.stackPane);*/
                    try {
                        objectWriter.writeObject(new DataPacket("newPrivateChat", users, null));
                        objectWriter.flush();
                        objectWriter.reset();
                    } catch (IOException e) {
                        e.printStackTrace(); 
                    }
                }
            }
        });

    	return directMessageWindow;
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
    	HBox IPBox = new HBox();
    	IPBox.setSpacing(14);
    	HBox usernameBox = new HBox();
    	usernameBox.setSpacing(18);
    	VBox vbox = new VBox();
    	vbox.setSpacing(10);
    	
    	GridPane signInGrid = setUpGridPane();
    	
    	Label IPAddressLabel = new Label();
    	IPAddressLabel.setText("IP-Address: ");
    	IPAddressBox = new TextField();
    	IPBox.getChildren().addAll(IPAddressLabel, IPAddressBox);
    	
    	Label usernameLabel = new Label();
    	usernameLabel.setText("Username: ");
    	TextField username = new TextField();
    	usernameBox.getChildren().addAll(usernameLabel, username);
    	
    	vbox.getChildren().addAll(IPBox, usernameBox);
    	
    	
    	  GridPane.setConstraints(vbox, 0, 6); 

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

                      List<String> clientUsername = new ArrayList<>();
                      clientUsername.add(clientID);
                     try {
                 		DataPacket data = new DataPacket("signIn",clientUsername, "");
                 		objectWriter.writeObject(data);
                 		Thread.sleep(500);
                 		setUpMainTabs(primaryStage);
                 		objectWriter.flush();
                 		objectWriter.reset();
                 	} catch (Exception e) {      	}

                     setUpMainTabs(primaryStage);
                  }
              }
          });
          signInGrid.getChildren().addAll(vbox);
    	return signInGrid;
    }
    
    private void showTabPane(TabPane tabPane, Stage primaryStage) {
    	Scene scene = new Scene(tabPane, 700, 505);
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
		HBox dmGrid = setUpDMTab();
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
        sock = new Socket(IPAddressBox.getText(), 5001);

        if (sock.isConnected()) {
            System.out.println("connection established");
        }
        objectReader = new ObjectInputStream(sock.getInputStream());
        objectWriter = new ObjectOutputStream(sock.getOutputStream());

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    class IncomingReader implements Runnable {
        @Override
        public void run() {
            Object m;
            try {
                // wait until Gui has initiated to receive messages
                while (sentMessages == null) {
                    Thread.sleep(1000);
                }
                while ((m = objectReader.readObject()) != null) {

                    DataPacket message = (DataPacket) m;
                    unpackData(message);
                }
                sock.close();
            } catch (IOException e) {
            	//e.printStackTrace();
            	}
            catch (ClassNotFoundException e) {e.printStackTrace();}
            catch (InterruptedException e) {e.printStackTrace();}
        }
    }

    class UsersOnlineUpdater implements Runnable {
        @Override
        public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    usersOnline.setItems(usernamesOnline);
                }
            });
        }
    }

    public void unpackData(DataPacket data) {
        String type = data.type;
        List<String> recipients = data.recipients;
        String message = data.message;

        if (type.equals("usersOnNetwork")) {
            usernamesOnline = FXCollections.observableArrayList(recipients);
            Thread updateUsers = new Thread(new UsersOnlineUpdater());
            updateUsers.start();
            //TODO:remove
            System.out.println(recipients);
        } else if (type.equals("public")) {
            sentMessages.appendText(message + newLine);
        } else if (type.equals("private")) {
            Set<String> recipientsNoOrder = new HashSet<>(recipients);
            Conversation c1 = conversations.get(recipientsNoOrder); //TODO: fix this so that order doesn't matter
            c1.sentMessages.appendText(message + newLine);
        } else if (type.equals("newPrivateChat")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ConversationLabel c1 = new ConversationLabel(directMessageWindow, objectWriter, recipients, clientID);
                            conversations.put((new HashSet<>(recipients)), c1.convo);
                            conversationList.getChildren().add(c1.stackPane);
                        }
                    });
                }
            }).start();
        }
    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Client c1 = new Client();
        System.out.println("starting up client");
        
        c1.initiateGui(primaryStage);
        c1.setUpNetwork();
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }
}
