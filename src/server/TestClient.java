package server;

import utils.FileDeletionEvent;
import utils.FileEvent;
import utils.FileModificationEvent;

import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        Socket s = null;
        PrintWriter w = null;
        BufferedReader r = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            s = new Socket("localhost", 8000);
            w = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
            r = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            //-------------------
            // ESTE ORDEN IMPORTA
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
            //-------------------
            long unixTime = System.currentTimeMillis() / 1000L;

            //EJEMPLO: mandar un push
            w.println("PUSH");
            w.flush();
            oos.writeObject(new FileModificationEvent("hola.txt", unixTime));
            System.out.println("SERVER: " + r.readLine()); //DEBERÍA SER OK

            //EJEMPLO: hacer un getall
            w.println("GETALL");
            w.flush();
            String read;
            while(!(read = r.readLine()).equalsIgnoreCase("END")) {
                System.out.println(read);
            }

            //EJEMPLO: hacer un get
            w.println("GET hola.txt");
            w.flush();
            try {
                FileEvent event = (FileEvent) ois.readObject();
                System.out.println("EVENTO: " + event);
                //Como ver que tipo de evento es
                System.out.println("FileModificationEvent? " + (event instanceof FileModificationEvent)); //True
                System.out.println("FileDeletionEvent? " + (event instanceof FileDeletionEvent)); //False
                //Si es una modificacion, obtenemos las IPs con
                if (event instanceof FileModificationEvent event1) {
                    System.out.println("IPS de la modificacion");
                    for(String ip : event1.getIps()) {
                        System.out.print(ip+",");
                    }
                    System.out.println();
                }

                //Por aquí descargaríamos el archivo de otro cliente etc etc ...
                //descargarArchivo();
                //Por último, notificamos al servidor de que tenemos la última versión
                w.println("OK");
                w.flush();
            } catch(ClassNotFoundException ex) {
                System.err.println("Error al deserializar la clase");
            }

            r.close();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (s != null) s.close();
            } catch(IOException e) {
                System.out.println("ERROR CATASTROFICO");
            }
        }
    }
}
