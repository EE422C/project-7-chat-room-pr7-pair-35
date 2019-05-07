package ClientSide;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class Sounds {
    private static Media messageReceived = new Media(new File("messageReceived.mp3").toURI().toString());
    public static MediaPlayer messageReceivedPlayer = new MediaPlayer(messageReceived);
}
