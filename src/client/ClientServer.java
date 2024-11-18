package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientServer implements Runnable{
    private Socket socket = null;
    private final String dirRoute;
    private File dir;

    public ClientServer (Socket socket, String dirRoute) {
        this.socket = socket;
        this.dirRoute = dirRoute;
    }

    @Override
    public void run() {
        dir = new File(dirRoute);

        BufferedReader in = null;
        PrintWriter out = null;
        FileInputStream fis = null;
        OutputStream os = null;

        try {
            os = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(os);

            String message = in.readLine();
            if (message.matches("^GET .*")) {
                String fName = message.split(" ")[1];
                File [] fNameList = dir.listFiles((file,name)->name.equals(fName));
                if (fNameList.length!=0) {                    
                    File file = fNameList[0];
                    fis = new FileInputStream(file);

                    out.println("OK");
                    out.flush();

                    byte [] buff = new byte[2048];
                    int read = fis.read(buff);
                    while(read!=-1) {
                        os.write(buff, 0, read);
                        read = fis.read(buff, 0, read);
                    }
                    os.flush();

                    fis.close();
                } else {
                    out.println("ERROR");
                    out.flush();
                }
            }

            in.close();
            out.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
}
