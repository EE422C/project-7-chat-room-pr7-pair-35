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

import java.io.Serializable;
import java.util.List;

public class DataPacket implements Serializable {
    public String type;
    public List<String> recipients;
    public String message;

    public DataPacket(String type, List<String> recipients, String message) {
        this.type = type;
        this.recipients = recipients;
        this.message = message;
    }
}
