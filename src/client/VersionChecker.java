package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.TimerTask;

public class VersionChecker extends TimerTask{
    private String dirRoute;

    public VersionChecker(String dir) {
        this.dirRoute = dir;
    }

    @Override
    public void run() {
        long lastModified;
        File dir = new File(dirRoute);
        for (File file : dir.listFiles()) {
            // Lo primero es pedir al servidor la última fecha de modificación para compararla con la del Cliente
            try (Socket socket = new Socket(Client.getServerHost(), Client.getServerPort());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());
            ){
                out.println("GET "+file.getName());
                out.flush();
                String res = in.readLine(); 
                if (res==null) {

                } else if (res.startsWith("FILE")) { // Caso en el que el servidor tenga alguna versión del archivo
                    lastModified = Long.parseLong(res.split(" ")[2]);
                    if (lastModified<file.lastModified()) { // Si el archivo es una versión más nueva que la del servidor
                        notifyChanges(file.getName(), file.lastModified());
                    } else if (lastModified>file.lastModified()){ // Si el archivo es una versión más vieja que la del servidor
                        // Pedir a otro Cliente de la lista de IPs el archivo
                    }
                } else if (res.startsWith("DELETE")){
                    file.delete();
                } else if (res.startsWith("ERROR")) {
                    // Manejar el error
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }





        
		lastModified = dir.lastModified();
		
    }

    private void notifyChanges (String fileName, long lastModified) {
        try {
            // ¿Socket y los Streams pueden ser compartidos por toda la clase?
            out.println("PUSH "+ fileName +" "+lastModified);
            out.flush();

            if (in.readLine().startsWith("ERROR")) {
                // Manejar el error
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyDelete (long lastModified) {

    }
}
