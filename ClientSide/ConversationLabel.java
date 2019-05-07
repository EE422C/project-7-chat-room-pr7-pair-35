package ClientSide;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConversationLabel {
    Rectangle msg;
    Text text;
    StackPane stackPane;
    HBox parentBox;
    Conversation convo;

    String clientID;

    String convoMembers;

    public ConversationLabel(HBox hBox, ObjectOutputStream out, List<String> convoMems, String clientID) {
        this.clientID = clientID;

        List<String> convoMemsReordered = new ArrayList<>();
        convoMemsReordered.add(clientID);

        for (String s : convoMems) {
            if (!s.equals(clientID)) {
                convoMemsReordered.add(s);
            }
        }

        convo = new Conversation(convoMemsReordered, out);

        convoMembers = convo.convoMembers.toString().replace("[","").replace("]","");


        parentBox = hBox;

        msg = new Rectangle(200,50);
        msg.setStroke(Color.GRAY);
        msg.setFill(Color.WHITE);
        text = new Text(convoMembers);
        stackPane = new StackPane(msg, text);
        stackPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    parentBox.getChildren().set(2, convo.grid);  // throws exception if no convos have been opened yet
                } catch (Exception e) {
                    parentBox.getChildren().add(convo.grid);    // in which case just add the convo to parentBox
                }
            }
        });
    }
}
