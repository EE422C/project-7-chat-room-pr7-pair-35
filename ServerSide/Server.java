package ServerSide;

//import ClientSide.DataPacket;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

import ClientSide.DataPacket;

public class Server extends Observable {
    private Map<String, ObjectOutputStream> clientOutputStream;
    private int clientNum;

    public void setUpServer() throws IOException {
        System.out.println("starting up server");

        ServerSocket serverSock = new ServerSocket(5001);

        clientOutputStream = new HashMap<>();

        while (true) {
            Socket clientSock = serverSock.accept();
            System.out.println("connection established");
            String ip = clientSock.getInetAddress().getHostAddress();    // get ip address of client
            ClientObserver writer = new ClientObserver(clientSock.getOutputStream());
            Thread t = new Thread(new ClientHandler(clientSock, ip));
            t.start();
            this.addObserver(writer);
            clientNum = this.countObservers();
            clientOutputStream.put(ip, writer);

        }
    }

    class ClientHandler implements Runnable {
        private ObjectInputStream objectReader;
        private ObjectOutputStream clientWriter;
        //Socket client;
        private String clientIp;

        public ClientHandler(Socket clientSock, String clientIp) throws IOException {
            Socket client = clientSock;
            objectReader = new ObjectInputStream(client.getInputStream());
            this.clientIp = clientIp;
        }

        @Override
        public void run() {
            Object m;
            try {
                while (( m = objectReader.readObject()) != null) {
                    synchronized (this) {    // correct sync placement?
                       DataPacket data = (DataPacket) m;
                       unpackData(data, clientIp);
                    }
                }
            } catch (IOException e) {
                deleteObservers();
                System.out.println("client has disconnected");
            }
            catch (ClassNotFoundException e) {e.printStackTrace();}
        }
    }

    private void sendDirectMessage(String address, String message) {
        ObjectOutputStream clientStream = clientOutputStream.get(address);
        try {
            clientStream.writeObject(message);
        } catch (IOException e) {e.printStackTrace();}
    }

    public void sendUsersList(String address, List<String> usernames) {
        System.out.println(address);
        ObjectOutputStream clientStream = clientOutputStream.get(address);
        String userListCSV = "";
        Iterator i = usernames.iterator();
        while (i.hasNext()) {
            userListCSV += i.next();
            if (i.hasNext()) {
                userListCSV += ",";
            }
        }
        System.out.println(userListCSV);
        try {
            clientStream.writeObject(userListCSV);
        } catch (IOException e) {e.printStackTrace();}
    }

    private void unpackData(DataPacket data, String senderIp) {
        String type = data.type;
        String[] recipients = data.recipients;
        String msg = data.message;

        if (type.equals("public")) {
            synchronized (this) {
                String message = recipients[0] + ": " + msg;    // add username to front of message
                setChanged();
                notifyObservers(message);
                System.out.println("public chat works");
            }
        } else if (type.equals("private")) {
            try {
                for (int i = 0; i < data.recipients.length; i++) {
                    Database.User user =Database.getUserFromDatabase(recipients[i], Database.DATABASE_URL);
                    String message = recipients[0] + ": " + msg;
                    sendDirectMessage(user.getIpAddress(), message);
                }
            } catch (Exception e) {e.printStackTrace();}
            System.out.println("private chat works");
        } else if (type.equals("usersOnNetwork")) {
            List<String> usernames = new ArrayList<>();
            try {
                usernames = Database.getAllUsers(Database.DATABASE_URL);
                Database.User user = Database.getUserFromDatabase(recipients[0], Database.DATABASE_URL);
                sendUsersList(user.getIpAddress(), usernames);
            } catch (Exception e) {e.printStackTrace();};
        } else if (type.equals("signIn")) {
            try {
                // if user not already in database, add them
                Database.User user = Database.getUserFromDatabase(recipients[0], Database.DATABASE_URL);
                if (user == null) {
                    Database.addUsertoDatabase(new Database.User(recipients[0],null ,senderIp), Database.DATABASE_URL);
                }
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().setUpServer();
    }
}