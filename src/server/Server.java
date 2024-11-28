package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static UpdateLog log = new UpdateLog();

    public static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        Scanner k = new Scanner(System.in);
        ClientAcceptor a = new ClientAcceptor();
        System.out.println("Starting listening on port 8000.");
        pool.submit(a);
        System.out.println("Started listening on port 8000");
        String sel;
        do {
            System.out.print("Menú de servidor. Introduce opción\n 1. Mostrar registro de actualización\n 2. Salir\n>");
            sel = k.nextLine();
            if (sel.equalsIgnoreCase("1")) {
                log.getFiles().forEach(System.out::println);
            }
        } while (!sel.equalsIgnoreCase("2"));
        System.out.println("Apagado servidor. Saliendo.");
        pool.shutdownNow();
    }
    public static class ClientAcceptor extends Thread {
        private ServerSocket server = null;
        public void run() {
            try {
                server = new ServerSocket(8000);
                server.setSoTimeout(10*1000);
                while (!Thread.interrupted()) {
                    try {
                        Socket client1 = server.accept();
                        pool.submit(new ClientHandler(client1));
                    }
                    catch (SocketTimeoutException ex) {
                    	
                    }
                    catch (IOException e) {
                        System.out.println("[SERVER THREAD] Error while handling client");
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                if (server != null) {
                    try {
                        server.close();
                    } catch(IOException e) {
                        System.err.println("[SERVER THREAD] ERROR CATASTROFICO");
                    }
                }
            }
        }
    }
}
