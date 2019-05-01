package ClientSide;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Application {

    public static final String newLine = System.lineSeparator();

    // GUI
    private GridPane grid;
    private TextArea sentMessages;
    private TextField messageBox;
    private Button sendBtn;
    //

    private BufferedReader reader;
    private PrintWriter writer;
    Scanner input;
    Socket sock;

    String clientID;

    public void setUpGridPane() {
        grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.addRow(100);
        grid.addColumn(100);
        grid.setVgap(1);
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
                String msg = messageBox.getText().replaceAll(newLine, "");
                if ((keyPressed.contains("\r")|| keyPressed.contains("\n") ||
                        keyPressed.contains(newLine)) && !msg.isEmpty()) {
                    writer.println(clientID + ": " + msg);
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
                String msg = messageBox.getText().replaceAll(newLine, "");
                if (!msg.isEmpty()) {
                    writer.println(clientID + ": " + msg);
                    messageBox.clear();
                }
            }
        });

    }

    public void initiateGui(Stage primaryStage) {
        setUpGridPane();
        setUpSentMessages();
        setUpMessageBox();
        setUpSendButton();

        grid.getChildren().addAll(sentMessages, messageBox, sendBtn);

        Scene scene = new Scene(grid, 470, 480);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Room");
        primaryStage.show();
    }

    public void setUpNetwork() throws IOException {
        input = new Scanner(System.in);
        sock = new Socket("localhost", 5001);

        if (sock.isConnected()) {
            System.out.println("connection established");
        }
        InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
        reader = new BufferedReader(streamReader);
        writer = new PrintWriter(sock.getOutputStream(), true);

        // get user ID from server
        clientID = "user" + reader.readLine();

        Thread readerThread = new Thread(new IncomingReader());
        Thread writerThread = new Thread(new OutgoingWriter());


        readerThread.start();
        writerThread.start();
    }

    class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                // wait until Gui has initiated to receive messages
                while (sentMessages == null) {
                    Thread.sleep(1000);
                }
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                    sentMessages.appendText(message + newLine);
                }
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    class OutgoingWriter implements Runnable {
        public void run() {
            Scanner scanner = new Scanner(System.in);
            String message;
            while (scanner.hasNext()) {
                message = scanner.nextLine();
                writer.println(message);
                sentMessages.appendText(clientID + ": " + message);
            }
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

    }
}
