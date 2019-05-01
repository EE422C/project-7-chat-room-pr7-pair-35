package ClientSide;

import java.io.Serializable;

public class Message implements Serializable {
    public String type;
    public String[] recipients;
    public String message;

    public Message(String type, String[] recipients, String message) {
        this.type = type;
        this.recipients = recipients;
        this.message = message;
    }
}
