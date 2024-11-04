package server;

import server.utils.UpdateLog;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    public static UpdateLog log = new UpdateLog();
    public static void main(String[] args) {
        ServerSocket server = null;
        Socket client = null;
        try {
            server = new ServerSocket(80000);
            System.out.println("Started listening on port 80000");
            while(true) {
                client = server.accept();
                ClientHandler handler = new ClientHandler(client);
                handler.start();
            }

        } catch(IOException e) {
            e.printStackTrace(); //TODO
        } finally {
            try {
                if (server != null) server.close();
            } catch(IOException e) {
                System.out.println("ERROR CATASTROFICO");
            }
        }
    }
}
