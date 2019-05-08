/* CHAT ROOM <MyClass.java>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Carlos Villapudua
 * civ298
 * 16190
 * David Day
 * dld2864
 * 16190
 * Slip days used: 3
 * Spring 2019
 */

package ServerSide;

import ClientSide.DataPacket;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

import ClientSide.DataPacket;

public class Server extends Observable {
    private Map<Socket, ObjectOutputStream> clientOutputStream;
    private Map<String, Socket> usersOnline;
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
            clientOutputStream.put(clientSock, writer);

        }
    }

    class ClientHandler implements Runnable {
        private ObjectInputStream objectReader;
        private ObjectOutputStream clientWriter;
        Socket client;
        private String clientIp;

        public ClientHandler(Socket clientSock, String clientIp) throws IOException {
            client = clientSock;
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
                       unpackData(data, clientIp, client);
                    }
                }
            } catch (Exception e) {

                // need to remove all instances of client on the server, including observer object
                String username = "";
                for (Map.Entry<String, Socket> s : usersOnline.entrySet()) {    // find socked using username
                    if (s.getValue().equals(client)) {
                        username = s.getKey();
                    }
                }
                usersOnline.remove(username);    // remove username from list of users online
                Observer o = (Observer) clientOutputStream.get(client);    // cast writer back to observer to delete
                Server.this.deleteObserver(o);
                clientOutputStream.remove(client);

                List<String> users = getUsersOnline();
                DataPacket data = new DataPacket("usersOnNetwork",users, null);
                synchronized (this) {    // notify clients of new list of users online after clients disconnects
                    setChanged();
                    notifyObservers(data);
                }
                System.out.println("client has disconnected");
            }
        }
    }

    private void sendDirectMessage(String user, DataPacket message) {
        ObjectOutputStream clientStream = clientOutputStream.get(usersOnline.get(user));
        try {
            clientStream.writeObject(message);
            clientStream.flush();
            clientStream.reset();
        } catch (IOException e) {e.printStackTrace();}
    }


    private List<String> getUsersOnline() {
        Collection<String> users = usersOnline.keySet();
        List<String> usersOnline = new ArrayList<>(users);

        return usersOnline;
    }

    private void unpackData(DataPacket data, String senderIp, Socket senderSock) {
        String type = data.type;
        List<String> recipients = data.recipients;
        String msg = data.message;

        if (type.equals("public")) {
            synchronized (this) {
                String message = recipients.get(0) + ": " + msg;    // add username to front of message
                setChanged();
                notifyObservers(new DataPacket("public", recipients, message));
                System.out.println("public chat works");
            }
        } else if (type.equals("private")) {
            try {
                for (int i = 0; i < recipients.size(); i++) {
                    Database.User user = Database.getUserFromDatabase(recipients.get(i), Database.DATABASE_URL);
                    System.out.println(user.getUsername() + " " + user.getIpAddress());
                    String message = recipients.get(0) + ": " + msg;
                    sendDirectMessage(recipients.get(i), new DataPacket("private", recipients, message));
                }
            } catch (Exception e) {e.printStackTrace();}
            System.out.println("private chat works");
        } else if (type.equals("signIn")) {
            try {
                // if user not already in database, add them
                Database.User user = Database.getUserFromDatabase(recipients.get(0), Database.DATABASE_URL);
                if (user == null) {
                    Database.addUsertoDatabase(new Database.User(recipients.get(0),null ,senderIp), Database.DATABASE_URL);
                }
                System.out.println(senderIp + " " + recipients.get(0));
                usersOnline.put(recipients.get(0), senderSock);
                List<String> users = getUsersOnline();
                synchronized (this) {    // notify clients of users online after new client connects
                    setChanged();
                    notifyObservers(new DataPacket("usersOnNetwork", users, null));
                }
            } catch (Exception e) {e.printStackTrace();}
        } else if (type.equals("newPrivateChat")) {
            System.out.println("request for private chat");
            try {
                for (int i = 0; i < recipients.size(); i++) {
                    sendDirectMessage(recipients.get(i), new DataPacket("newPrivateChat", recipients, null));
                }
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().setUpServer();
    }
}