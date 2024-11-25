package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private static long refreshRate; // Refresh rate in milliseconds
    private static String dirRoute;
    private static String serverHost;
    private static int serverPort = 8000;

    private static ExecutorService pool = Executors.newCachedThreadPool();

    public static void main (String [] args) {
        dirRoute = "D:\\Prueba OpenDrive";
		Calendar init = Calendar.getInstance();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new VersionChecker(dirRoute), init.getTime(), refreshRate);

        
        try (ServerSocket clientServerSocket = new ServerSocket(66666)) {
            while (true) {
                try {
                    Socket clientSocket = clientServerSocket.accept();
                    pool.execute(new ClientServer(clientSocket, dirRoute));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    public static String getServerHost  () {
        return serverHost;
    }

    public static int getServerPort  () {
        return serverPort;
    }
}
