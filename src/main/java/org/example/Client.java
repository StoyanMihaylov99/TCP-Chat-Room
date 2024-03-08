package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private static final String ADDRESS = "127.0.0.1";
    private static final String QUIT_COMMAND = "/quit";


    @Override
    public void run() {
        try {
            client = new Socket(ADDRESS,9999);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        } catch (IOException e){
            shutDown();
        }
    }

    public void shutDown(){
        try{
            done = true;
            out.close();
            in.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch (IOException e){
            //ignore
        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run(){
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inReader.readLine();
                    if(message.equals(QUIT_COMMAND)){
                        out.println(message);
                        inReader.close();
                        shutDown();
                    } else {
                        out.println(message);
                    }
                }
            }catch (IOException e){
                shutDown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
