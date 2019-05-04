package ServerSide;

//import ClientSide.DataPacket;
import java.io.*;
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
            ClientObserver writer = new ClientObserver(clientSock.getOutputStream());
            Thread t = new Thread(new ClientHandler(clientSock));
            t.start();
            this.addObserver(writer);
            clientNum = this.countObservers();
            clientOutputStream.put(clientSock.getInetAddress().getHostAddress(), writer);

            // safe to have here?
        }
    }

    class ClientHandler implements Runnable {
        private ObjectInputStream objectReader;

        public ClientHandler(Socket clientSock) throws IOException {
            Socket client = clientSock;
            objectReader = new ObjectInputStream(client.getInputStream());
        }

        @Override
        public void run() {
            Object m;
            try {
                while (( m = objectReader.readObject()) != null) {
                    synchronized (this) {    // correct sync placement?
                       DataPacket data = (DataPacket) m;
                       unpackData(data);
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

    private void sendUsersList(String address, List<String> usernames) {
        ObjectOutputStream clientStream = clientOutputStream.get(address);
        try {
            clientStream.writeObject(usernames);
        } catch (IOException e) {e.printStackTrace();}
    }

    private void unpackData(DataPacket data) {
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
                    String message = recipients[i] + ": " + msg;
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
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().setUpServer();
    }
}