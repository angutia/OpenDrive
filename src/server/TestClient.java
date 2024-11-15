package server;

import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        Socket s = null;
        PrintWriter w = null;
        BufferedReader r = null;
        try {
            s = new Socket("localhost", 8000);
            w = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
            r = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            long unixTime = System.currentTimeMillis() / 1000L;
            w.println("PUSH hola.txt " + (unixTime+1000L));
            w.flush();
            System.out.println("SERVER: " + r.readLine()); //DEBERÍA SER OK
            w.println("GETALL " + unixTime);
            w.flush();
            String read;
            while(!(read = r.readLine()).equalsIgnoreCase("END")) {
                System.out.println(read);
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
