package client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;

import client.gui.ConfigGUI;

public class Client {

    private static long refreshRate; // Refresh rate in milliseconds
    public static String dirRoute;
    public static String serverHost = "localhost";
    public static int serverPort = 8000;
    
    private static Timer timer;
    private static TimerTask task;
    private static ConfigGUI gui;

    private static ExecutorService pool = Executors.newCachedThreadPool();
    
    private static boolean close = false;

    public static void main (String [] args) {
        dirRoute = "C:\\Users\\PcBox\\Desktop\\cliente1";
        refreshRate=30000;
		
		setupTray();
		
		ConfigGUI.setLookAndFeel();
		gui = new ConfigGUI();
		gui.setVisible(true);
		
		timer = new Timer();
		recreateTimerTask();
		
		
        log("Finished client startup.");
        try (ServerSocket clientServerSocket = new ServerSocket(6666)) {
            while (true && !close) {
                try {
                	clientServerSocket.setSoTimeout(10*1000);
                	//log("Waiting for clients.");
                    Socket clientSocket = clientServerSocket.accept();
                    pool.execute(new ClientServer(clientSocket, dirRoute));
                } catch(SocketTimeoutException e) {
                	//Do nothing
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            timer.cancel();
            task.cancel();
        }
        log("Exited");
    }

    public static void setupTray() {
    	if (!SystemTray.isSupported()) {
    		return;
    	}
    	PopupMenu menu = new PopupMenu();
    	SystemTray tray = SystemTray.getSystemTray();
    	TrayIcon icon = new TrayIcon(createImage("/cloud.png", "tray icon"));
    	
    	MenuItem config = new MenuItem("Configuración");
    	MenuItem exit = new MenuItem("Salir");

    	menu.add(config);
    	menu.addSeparator();
    	menu.add(exit);
    	config.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client.gui.setVisible(true);
			}
		});
    	exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client.gui.dispose();
				Client.close=true;
				tray.remove(icon);
			}
		});
    	
    	icon.setImageAutoSize(true);
    	icon.setPopupMenu(menu);
    	
    	try {
            tray.add(icon);
        } catch (AWTException e) {
            log("[ERROR] TrayIcon could not be added.");
            return;
        }
    	
    }
    
    protected static Image createImage(String path, String description) {
        URL imageURL = Client.class.getResource(path);
         
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
    
    public static String getServerHost() {
        return serverHost;
    }

    public static int getServerPort() {
        return serverPort;
    }
    
    public static void setRefreshRate(long rate) {
    	refreshRate = rate;
    	recreateTimerTask();
    }
    
    private static void recreateTimerTask() {
    	//timer.cancel();
    	if (task!=null) task.cancel();
    	task = new VersionChecker(dirRoute);
      	timer.scheduleAtFixedRate(task, Calendar.getInstance().getTime(), refreshRate);
    }
    
    public static long getRefreshRate() {
    	return refreshRate;
    }
    
    public static void log(String line) {
    	//System.out.println(Calendar.getInstance().getTime() + " " + line);
    	gui.addLog(Calendar.getInstance().getTime() + " " + line);
    }
}
