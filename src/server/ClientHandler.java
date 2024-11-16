package server;

import utils.FileEvent;
import utils.FileModificationEvent;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    public void writeAllFiles(PrintWriter writer) throws IOException{
        List<FileEvent> files = Server.log.getFiles();
        for (FileEvent f : files) {
            writer.println(f.toString());
        }
        writer.println("END");
    }

    public boolean writeFile(ObjectOutputStream oos, String file) throws IOException {
        FileEvent tosend = Server.log.getFileEventByName(file);
        if (tosend == null) return false;
        oos.writeObject(tosend);
        oos.flush();
        return true;
    }

    public void handleClient() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(this.client.getOutputStream(), StandardCharsets.UTF_8));
        ObjectInputStream ois = new ObjectInputStream(this.client.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(this.client.getOutputStream());
        String read;
        while(!(read = reader.readLine()).matches("EXIT")) {

            if (read.matches("^GETALL$")) {
                this.writeAllFiles(writer);
            }
            else if (read.matches("^GET .*")) {
                String sendStr = read.replace("GET ", "");
                boolean found = this.writeFile(oos, sendStr);
                if (!found) writer.println("ERROR FILE NOT FOUND");
                else {
                    String response = reader.readLine(); //READ THE OK OR ERROR
                    if (!response.equalsIgnoreCase("OK")) {
                        //TODO better error handling?
                        System.err.println("[ClientHandler] Client sent error '" + response + "'");
                        continue;
                    }
                    //Add the client ip to the list of updated clients
                    Server.log.addIP(sendStr, this.client.getInetAddress().getHostAddress());
                }
            } else if (read.matches("^PUSH$")) {
                try {
                    FileEvent event = (FileEvent) ois.readObject();
                    Server.log.pushUpdate(event, this.client.getInetAddress().getHostAddress());
                    writer.println("OK");
                } catch(ClassNotFoundException e) {
                    writer.println("ERROR CLASS NOT FOUND");
                }
            }
            writer.flush();
        }

    }

}
