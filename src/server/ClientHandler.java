package server;

import server.utils.File;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

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

    public void writeAllFiles(PrintWriter writer, long timestamp) throws IOException{
        List<File> files = Server.log.getFilesNewerThan(timestamp);
        for (File f : files) {
            writer.println("FILE " + f.toString());
        }
        writer.println("END");
    }

    public void writeFile(PrintWriter writer, String file) throws IOException {
        String tosend = Server.log.getFileByName(file).toString();
        if (!file.isEmpty()) {
            writer.println("FILE " + tosend);
        } else {
            writer.println("ERROR FILE NOT FOUND");
        }
    }

    public void handleClient() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(this.client.getOutputStream(), StandardCharsets.UTF_8));
        //TODO: Quizá podríamos mandar el objeto File serializado?
        String read;
        while(!(read = reader.readLine()).matches("EXIT")) {
            if (read.matches("^GETALL .*")) {
                String time = read.replace("GETALL ", "");
                long timestamp;
                try {
                    timestamp = Long.parseLong(time);
                    this.writeAllFiles(writer, timestamp);
                } catch(NumberFormatException e) {
                    writer.println("ERROR TIME SHOULD BE A UNIX TIMESTAMP");
                }
            }
            else if (read.matches("^GET .*")) {
                String sendStr = read.replace("GET ", "");
                this.writeFile(writer, sendStr);

            } else if (read.matches("PUSH .* .*")) {
                String filename = read.split(" ")[1];
                String newTime = read.split(" ")[2];
                long time;
                try {
                    time = Long.parseLong(newTime);
                    long updatedTime = Server.log.pushUpdate(time, filename, this.client.getInetAddress().getHostAddress());
                    if (updatedTime==time) {
                        writer.println("OK");
                    } else {
                        writer.println("ERROR NEWER VERSION AVAILABLE");
                    }
                } catch(NumberFormatException e) {
                    writer.println("ERROR TIME SHOULD BE A UNIX TIMESTAMP");
                }
            }
            writer.flush();
        }

    }

}
