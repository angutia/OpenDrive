package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.gui.ConfigGUI;

public class Client {

    public static long refreshRate; // Refresh rate in milliseconds
    public static String dirRoute;
    public static String serverHost;
    public static int serverPort = 8000;
    
    private static ConfigGUI gui;

    private static ExecutorService pool = Executors.newCachedThreadPool();

    public static void main (String [] args) {
        dirRoute = "E:\\Prueba OpenDrive";
        refreshRate=30000;
		Calendar init = Calendar.getInstance();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new VersionChecker(dirRoute), init.getTime(), refreshRate);
		
		gui = new ConfigGUI();
		ConfigGUI.setLookAndFeel();
		gui.setVisible(true);
        log("Finished client startup.");
        try (ServerSocket clientServerSocket = new ServerSocket(6666)) {
            while (true) {
                try {
                	log("Waiting for clients.");
                    Socket clientSocket = clientServerSocket.accept();
                    pool.execute(new ClientServer(clientSocket, dirRoute));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	log("Exiting...");
            pool.shutdown();
        }
        log("Exited");
    }

    public static String getServerHost  () {
        return serverHost;
    }

    public static int getServerPort  () {
        return serverPort;
    }
    
    public static void log(String line) {
    	System.out.println(line);
    	gui.addLog(line);
    }
}
