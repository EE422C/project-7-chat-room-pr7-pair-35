package ClientSide;

import java.io.Serializable;

public class DataPacket implements Serializable {
    public String type;
    public String[] recipients;
    public String message;

    public DataPacket(String type, String[] recipients, String message) {
        this.type = type;
        this.recipients = recipients;
        this.message = message;
    }
}
