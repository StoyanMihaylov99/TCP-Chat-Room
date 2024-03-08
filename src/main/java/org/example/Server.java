package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    public static final String INSERT_NICKNAME = "Please enter a nickname: ";
    public static final String SUCCESSFULLY_CONNECTED = " connected!";
    public static final String JOINED = " joined the chat!";
    public static final String NICKNAME_COMMAND = "/nick ";
    public static final String CHANGED_NICKNAME = " renamed themselves to ";
    public static final String SUCCESSFULLY_CHANGED = "Successfully changed nickname to ";
    public static final String NO_NICKNAME_PROVIDED = "No nickname provided!";
    public static final String QUIT_COMMAND = "/quit";
    public static final String LEFT_THE_CHAT = " left the chat.";

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;


    public Server() {
        connections = new ArrayList<>();
        done = false;
    }


    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutDownServer();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutDownServer() {
        try {
            pool.shutdown();
            done = true;
            if (!server.isClosed()) {
                server.close();
            }

            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignore
        }
    }


    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;


        public ConnectionHandler(Socket client) throws NoSuchAlgorithmException {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println(INSERT_NICKNAME);
                nickname = in.readLine();
                System.out.println(nickname + SUCCESSFULLY_CONNECTED);
                broadcast(nickname + JOINED);
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith(NICKNAME_COMMAND)) {
                        String[] messageSpit = message.split(" ", 2);
                        if (messageSpit.length == 2) {
                            broadcast(nickname + CHANGED_NICKNAME + messageSpit[1]);
                            System.out.println(nickname + CHANGED_NICKNAME + messageSpit[1]);
                            nickname = messageSpit[1];
                            out.println(SUCCESSFULLY_CHANGED + nickname);
                        } else {
                            out.println(NO_NICKNAME_PROVIDED);
                        }
                    } else if (message.startsWith(QUIT_COMMAND)) {
                        broadcast(nickname + LEFT_THE_CHAT);
                        shutdown();

                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //ignore;
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

