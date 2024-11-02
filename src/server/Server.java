package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static void main(String[] args) {
        ServerSocket server = null;
        Socket client = null;
        Logger logger = Logger.getLogger("Server");
        try {
            server = new ServerSocket(80000);
            logger.log(Level.INFO, "Started listening on port 80000");
            while(true) {
                client = server.accept();
                //TODO
            }

        } catch(IOException e) {
            e.printStackTrace(); //TODO
        }
    }
}
