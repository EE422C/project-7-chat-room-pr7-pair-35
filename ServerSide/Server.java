package ServerSide;

import ClientSide.Client;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Observable {
    private PrintWriter writer;
    private Map<String, PrintWriter> clientOutputStream;
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
            writer.println(clientNum);  // return connection number to client
            notifyClients("user" + clientOutputStream.size() + " has joined.");
        }
    }

    public void notifyClients(String message) {
        Set<Map.Entry<String, PrintWriter>> clients = clientOutputStream.entrySet();
        for (Map.Entry<String, PrintWriter> i : clients) {
            i.getValue().println(message);
        }
    }

    public void sendDirectMessage(String address, String message) {
        PrintWriter clientStream = clientOutputStream.get(address);
        clientStream.println(message);
    }

    class ClientHandler implements Runnable {
        private BufferedReader reader;

        public ClientHandler(Socket clientSock) throws IOException {
            Socket client = clientSock;
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    synchronized (this) {    // correct sync placement?
                        System.out.println(message + System.lineSeparator());
                        setChanged();
                        notifyObservers(message);
                    }
                }
            } catch (IOException e) {e.printStackTrace();}
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().setUpServer();
    }
}