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
