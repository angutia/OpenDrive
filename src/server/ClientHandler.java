package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread{

    private Socket client;
    private IOException ex;


    public ClientHandler(Socket client) {
        this.client=client;
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch(IOException e) {
            this.ex=e;
        } finally {
            try {
                this.client.close();
            } catch(IOException ex) {
                System.err.println("ERROR CATASTROFICO");
            }
        }
    }

    public void handleClient() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(this.client.getOutputStream(), StandardCharsets.UTF_8));

        String read;
        while(!(read = reader.readLine()).matches("EXIT")) {
            if (read.matches("^GET .*")) {
                String sendStr = Server.log.getFileString(read.replace("GET ", ""));
                if (!sendStr.isEmpty()) {
                    writer.write("FILE " + sendStr);
                } else {
                    writer.write("ERROR FILE NOT FOUND");
                }
                writer.flush();
            } else if (read.matches("PUSH .* .*")) {
                String filename = read.split(" ")[1];
                String newTime = read.split(" ")[2];
                long time;
                try {
                    time = Long.parseLong(newTime);
                } catch(NumberFormatException e) {
                    writer.write("ERROR TIME SHOULD BE A UNIX TIMESTAMP");
                    writer.flush();
                    continue;
                }
                long updatedTime = Server.log.pushUpdate(time, filename, this.client.getInetAddress().getHostAddress());
                if (updatedTime==time) {
                    writer.write("OK");
                } else {
                    writer.write("ERROR NEWER VERSION AVAILABLE");
                }
                writer.flush();
            }
        }
    }
}
