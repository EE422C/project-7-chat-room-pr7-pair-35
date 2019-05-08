/* CHAT ROOM <MyClass.java>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Carlos Villapudua
 * civ398
 * 16190
 * David Day
 * dld2864
 * 16190
 * Slip days used: 1
 * Spring 2019
 */

package ClientSide;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class Sounds {
    private static Media messageReceived = new Media(new File("messageReceived.mp3").toURI().toString());
    public static MediaPlayer messageReceivedPlayer = new MediaPlayer(messageReceived);
}
