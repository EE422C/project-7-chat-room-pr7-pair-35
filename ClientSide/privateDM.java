package ClientSide;

import java.io.IOException;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import java.io.*;



public class privateDM {
	
	Integer uniqueNum;
	private TextField messageBox;
	private Button sendBtn;
	private TextArea privateMessages;
	public static final String newLine = System.lineSeparator();
	private String clientID;
	private ObjectOutputStream objectWriter;
	private String[] recipients;

	public Tab privateDM(String clientID, ObjectOutputStream objectWriter, Integer uniqueNum, ArrayList<String> recipients) {
		this.clientID = clientID;
		this.objectWriter = objectWriter;
		this.uniqueNum = uniqueNum;
		this.recipients = (String[]) recipients.toArray();
		
		Tab tab = new Tab();
		
		GridPane grid = new GridPane();							//set up grid
        grid.setPadding(new Insets(10,10,10,10));
        grid.addRow(100);
        grid.addColumn(100);
        grid.setVgap(1);
        
        privateMessages = new TextArea();				//set up area to update previously sent messages
        privateMessages.setEditable(false);
        privateMessages.setWrapText(true);
        GridPane.setConstraints(privateMessages,0,0);
        privateMessages.setMinWidth(450);
        privateMessages.setMinHeight(400);
        
        setUpMessageBox();
        setUpSendButton();
        
        grid.getChildren().addAll(privateMessages, messageBox, sendBtn);
        tab.setContent(grid);
        return tab;
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

                    	DataPacket data = new DataPacket("private", recipients, message);
                        data.uniqueNo = uniqueNum;
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
                        DataPacket data = new DataPacket("private", recipients, message);
                        data.uniqueNo = uniqueNum;
                        objectWriter.writeObject(data);
                        objectWriter.flush();
                        objectWriter.reset();
                        messageBox.clear();
                    } catch (IOException e) {e.printStackTrace();}
                }
            }
        });

    }

}

