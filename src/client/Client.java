package client;

import java.util.Calendar;
import java.util.Timer;

public class Client {

    private static long refreshRate; // Refresh rate in milliseconds
    private static String dirRoute;
    private static String serverHost;
    private static int serverPort = 80000;

    public static void main (String [] args) {
        dirRoute = "D:\\Prueba OpenDrive";
		Calendar init = Calendar.getInstance();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new VersionChecker(dirRoute), init.getTime(), refreshRate);
    }

    public static String getServerHost  () {
        return serverHost;
    }

    public static int getServerPort  () {
        return serverPort;
    }
}
