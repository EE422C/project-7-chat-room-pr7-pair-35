package ClientSide;

import java.io.Serializable;

public class DataPacket implements Serializable {
    //group chats/private messages are numbered starting from 0 -- null uniqueNo means the chat is public
	public Integer uniqueNo;
	public String type;
    public String[] recipients;
    public String message;

    public DataPacket(String type, String[] recipients, String message) {
        this.type = type;
        this.recipients = recipients;
        this.message = message;
    }
}
