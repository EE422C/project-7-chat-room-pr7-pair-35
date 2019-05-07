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
    private Map<String, String> usersOnline;
    private int clientNum;

    public void setUpServer() throws IOException {
        System.out.println("starting up server");

        ServerSocket serverSock = new ServerSocket(5001);

        clientOutputStream = new HashMap<>();

        usersOnline = new HashMap<>();

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
            } catch (Exception e) {

                // need to remove all instances of client on the server, including observer object
                usersOnline.remove(clientIp);    // remove username from list of users online
                Observer o = (Observer) clientOutputStream.get(clientIp);    // cast writer back to observer to delete
                Server.this.deleteObserver(o);
                clientOutputStream.remove(clientIp);

                String users = getUsersOnline();
                synchronized (this) {    // notify clients of new list of users online after clients disconnects
                    setChanged();
                    notifyObservers(users);
                }
                System.out.println("client has disconnected");
            }
        }
    }

    private void sendDirectMessage(String address, DataPacket message) {
        ObjectOutputStream clientStream = clientOutputStream.get(address);
        try {
            clientStream.writeObject(message);
        } catch (IOException e) {e.printStackTrace();}
    }

    /*public void sendUsersList(String address, List<String> usernames) {
        System.out.println(address);
        ObjectOutputStream clientStream = clientOutputStream.get(address);
        String userListCSV = "usersOnNetwork/";
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
    }*/

    public String getUsersOnline() {
        Collection<String> users = usersOnline.values();
        String usersOnlineString = "";
        Iterator i = users.iterator();
        while (i.hasNext()) {
            usersOnlineString += i.next();
            if (i.hasNext()) {
                usersOnlineString += ",";
            }
        }
        return usersOnlineString;
    }

    private void unpackData(DataPacket data, String senderIp) {
        String type = data.type;
        String[] recipients = data.recipients;
        String msg = data.message;

        if (type.equals("public")) {
            synchronized (this) {
                String message = recipients[0] + ": " + msg;    // add username to front of message
                setChanged();
                notifyObservers(new DataPacket("public", recipients, message));
                System.out.println("public chat works");
            }
        } else if (type.equals("private")) {
            try {
                for (int i = 0; i < data.recipients.length; i++) {
                    Database.User user =Database.getUserFromDatabase(recipients[i], Database.DATABASE_URL);
                    String message = recipients[0] + ": " + msg;
                    sendDirectMessage(user.getIpAddress(), new DataPacket("private", recipients, message));
                }
            } catch (Exception e) {e.printStackTrace();}
            System.out.println("private chat works");
        } /*else if (type.equals("usersOnNetwork")) {
            List<String> usernames = new ArrayList<>();
            try {
                usernames = Database.getAllUsers(Database.DATABASE_URL);
                Database.User user = Database.getUserFromDatabase(recipients[0], Database.DATABASE_URL);
                sendUsersList(user.getIpAddress(), usernames);
            } catch (Exception e) {e.printStackTrace();};
        } */else if (type.equals("signIn")) {
            try {
                // if user not already in database, add them
                Database.User user = Database.getUserFromDatabase(recipients[0], Database.DATABASE_URL);
                if (user == null) {
                    Database.addUsertoDatabase(new Database.User(recipients[0],null ,senderIp), Database.DATABASE_URL);
                }
                System.out.println(senderIp + " " + recipients[0]);
                usersOnline.put(senderIp, recipients[0]);
                String users = getUsersOnline();
                synchronized (this) {    // notify clients of users online after new client connects
                    setChanged();
                    notifyObservers(new DataPacket("usersOnNetwork", recipients, users));
                }
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().setUpServer();
    }
}