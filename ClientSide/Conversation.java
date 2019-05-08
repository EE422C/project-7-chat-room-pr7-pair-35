/* CHAT ROOM <MyClass.java>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Carlos Villapudua
 * civ298
 * 16190
 * David Day
 * dld2864
 * 16190
 * Slip days used: 3
 * Spring 2019
 */

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Conversation {

    public static final String newLine = System.lineSeparator();

    List<String> convoMembers;

    ObjectOutputStream objectWriter;

    TextArea sentMessages;
    TextField messageBox;
    Button sendBtn;
    GridPane grid;


    public Conversation(List<String> convoMembers, ObjectOutputStream out) {

        objectWriter = out;
        this.convoMembers = convoMembers;

        grid = new GridPane();

        sentMessages = new TextArea();
        sentMessages.setWrapText(true);
        GridPane.setConstraints(sentMessages,0,0);
        sentMessages.setMinWidth(380);
        sentMessages.setMinHeight(400);

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
                        DataPacket data = new DataPacket("private", convoMembers, message);
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

        sendBtn = new Button("send");
        sendBtn.setMinWidth(50);
        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = messageBox.getText().replaceAll(newLine, "");
                if (!message.isEmpty()) {
                    try {
                        DataPacket data = new DataPacket("private",convoMembers, message);
                        objectWriter.writeObject(data);
                        objectWriter.flush();
                        objectWriter.reset();
                        messageBox.clear();
                    } catch (IOException e) {e.printStackTrace();}
                }
            }
        });

        GridPane.setConstraints(sendBtn, 1, 10);
        grid.setPadding(new Insets(10,10,10,10));
        grid.addRow(100);
        grid.addColumn(100);
        grid.setVgap(1);
        sendBtn.setTranslateX(-50);

        grid.getChildren().addAll(sentMessages, messageBox, sendBtn);
    }
}
