package client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;

import client.gui.ConfigGUI;

public class Client {
    private static String OS = (System.getProperty("os.name")).toUpperCase();
    private static String appDataRoute = null;
    private static Properties properties = new Properties();
    private static File configDir = null;
    private static File configFile = null;

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

        initializeConfigClient();
		
		ConfigGUI.setLookAndFeel();
		gui = new ConfigGUI();
		gui.setVisible(true);
		
		timer = new Timer();
		recreateTimerTask();
		
		
        log("Cliente inicializado.");
        try (ServerSocket clientServerSocket = new ServerSocket(6666)) {
        	clientServerSocket.setSoTimeout(10*1000);
        	while (true && !close) {
                try {
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

    public static void configClient() {
        log("Guardando configuración cliente...");

        if (OS.contains("WIN")) {
            appDataRoute = System.getenv("AppData");            
        } else {
            appDataRoute = ".";
        }

        if (appDataRoute!=null){
            Path configRoute = Path.of(appDataRoute, "OpenDrive");
            configDir = new File(configRoute.toString());
            configFile = new File(configRoute.toString(),"config.properties");
        }

        if (configFile==null) {
            log("Error: No se encontró la ruta predefinida para el archivo de configuración del cliente.");
            return;
        }
        
        if (!configFile.isFile()) {
            if (!configDir.isDirectory()) {
                configDir.mkdir();
            }
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                log("Error al crear el archivo config.properties: "+e.getLocalizedMessage());
            }
        }

        try (
            FileOutputStream fos = new FileOutputStream(configFile);
        ){
            properties.setProperty("refreshRate", String.valueOf(refreshRate));
            properties.setProperty("dirRoute", dirRoute);
            properties.setProperty("serverHost", serverHost);
            properties.setProperty("serverPort", String.valueOf(serverPort));

            properties.store(fos, appDataRoute);

        } catch (IOException e) {
            log("Error al inicializar la configuración del cliente: "+e.getLocalizedMessage());
        }
        
    }

    private static void initializeConfigClient() {
        System.out.println("Inicializando configuración cliente...");

        if (OS.contains("WIN")) {
            appDataRoute = System.getenv("AppData");            
        } else {
            appDataRoute = ".";
        }
        
        if (appDataRoute!=null){
            Path configRoute = Path.of(appDataRoute, "OpenDrive");
            configDir = new File(configRoute.toString());
            configFile = new File(configRoute.toString(),"config.properties");
        }

        if (configFile==null) {
            System.out.println("Error: No se encontró la ruta predefinida para el archivo de configuración del cliente.");
            return;
        }
        
        if (configFile.isFile()) {
            try (
                FileInputStream fis = new FileInputStream(configFile);
            ){
                properties.load(fis);
                refreshRate = Long.parseLong(properties.getProperty("refreshRate"));
                dirRoute = properties.getProperty("dirRoute");
                serverHost = properties.getProperty("serverHost");
                serverPort = Integer.parseInt(properties.getProperty("serverPort"));

            } catch (IOException e) {
                System.out.println("Error al inicializar la configuración del cliente: "+e.getLocalizedMessage());
            }
        } else {
            if (!configDir.isDirectory()) {
                configDir.mkdir();
            }

            FileOutputStream fos = null;
            try {
                configFile.createNewFile();
                fos = new FileOutputStream(configFile);

                properties.setProperty("refreshRate", String.valueOf(refreshRate));
                properties.setProperty("dirRoute", dirRoute);
                properties.setProperty("serverHost", serverHost);
                properties.setProperty("serverPort", String.valueOf(serverPort));

                properties.store(fos, appDataRoute);

                fos.close();
            } catch (IOException e) {
                System.out.println("Error al crear el archivo de configuración del cliente: "+e.getLocalizedMessage());
            } finally {
                if (fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        System.out.println("Error al cerrar el archivo de configuración: "+e.getLocalizedMessage());
                    }
                }
            }
        }
    }
}
