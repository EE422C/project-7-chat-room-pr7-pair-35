package ServerSide;

import ClientSide.DataPacket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
            setChanged();
            notifyObservers(clientNum);
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
                e.printStackTrace();}
            catch (ClassNotFoundException e) {e.printStackTrace();}
        }
    }

    private void sendDirectMessage(String address, String message) {
        ObjectOutputStream clientStream = clientOutputStream.get(address);
        try {
            clientStream.writeObject(message);
        } catch (IOException e) {e.printStackTrace();}
    }

    private void unpackData(DataPacket data) {
        String type = data.type;
        String msg = data.message;

        if (type.equals("public")) {
            synchronized (this) {
                setChanged();
                notifyObservers(msg);
                System.out.println("public chat works");
            }
        } else if (type.equals("private")) {
            for (int i = 0; i < data.recipients.length; i++) {
                sendDirectMessage(data.recipients[i], msg);
            }
            System.out.println("private chat works");
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().setUpServer();
    }
}