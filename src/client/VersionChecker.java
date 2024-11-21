package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TimerTask;
import utils.FileDeletionEvent;
import utils.FileEvent;
import utils.FileModificationEvent;

public class VersionChecker extends TimerTask{
    private String dirRoute;
    private File dir;

    public VersionChecker(String dir) {
        this.dirRoute = dir;
    }

    @Override
    public void run() {
        dir = new File(dirRoute);
        List<String> files = Arrays.asList(dir.list());

        // Lo primero es pedir al servidor la última actualización de todos los archivos de la carpeta
        try (Socket socket = new Socket(Client.getServerHost(), Client.getServerPort());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        // ESTE ORDEN IMPORTA
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ){
            out.println("GETALL");
            out.flush();
            String res = in.readLine();

            // Para cada actualización, comprobar si es una modificación o un borrado
            while (res!=null && !res.startsWith("END")) {
                String fName = res.split(" ")[1];
                File [] fNameList = dir.listFiles((file,name)->name.equals(fName));              
                if (res.startsWith("MODIFICATION")) {
                    if (files.contains(fName)) { // En el caso de que ya exista el archivo en la carpeta Cliente
                        long lastModifiedClient = fNameList[0].lastModified();
                        long lastModifiedServer = Long.parseLong(res.split(" ")[2]);
                        if (lastModifiedServer<lastModifiedClient) { // Si el archivo del Cliente es una versión más nueva que la del Servidor
                            files.remove(fName);
                            notifyModification(fName, lastModifiedClient, in, out, oos);
                        } else if (lastModifiedServer>lastModifiedClient){ // Si el archivo es una versión más vieja que la del Servidor
                            // Hacer un GET y conseguir el archivo
                        }
                    } else { // En el caso de que no exista el archivo en la carpeta Cliente
                        // ¿Cómo se sabe si lo tengo que crear porque no lo tenía (GET) o lo he borrado y tengo que avisar del borrado (PUSH)?

                        // Hacer un GET y conseguir el archivo
                        files.add(fName);

                        // Hacer un PUSH y avisar del borrado
                    }                    
                } else if (res.startsWith("DELETION")) {
                    if (files.remove(fName)) { // En el caso de que exista en la carpeta Cliente el archivo que ha sido borrado
                        // Manejar el borrado
                        fNameList[0].delete();
                    } else {
                        // En principio nada, el archivo ha sido borrado pero no estaba en la carpeta del cliente así que se queda igual
                    } 
                }
                res = in.readLine();
            }
            
            // Hacer un GET de cada archivo en files
            //TODO: solo hacer GET si es un archivo modificado por otro cliente
            for (String fName : files) {
                getFileEvent(fName, out, ois);
            }



        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean notifyModification (String fileName, long lastModified, BufferedReader in, PrintWriter out, ObjectOutputStream oos) throws IOException {
        FileEvent fileModification = new FileModificationEvent(fileName, lastModified);
        out.println("PUSH");
        out.flush();
        oos.writeObject(fileModification);
        oos.flush();

        String res = in.readLine();
        return !res.startsWith("ERROR");
    }

    private boolean notifyDelete (String fileName, long lastModified, BufferedReader in, PrintWriter out, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        FileEvent fileModification = new FileDeletionEvent(fileName, lastModified);
        out.println("PUSH");
        out.flush();
        oos.writeObject(fileModification);
        oos.flush();

        String res = in.readLine();
        return !res.startsWith("ERROR");
    }

    private void getFileEvent (String fName, PrintWriter out, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        out.println("GET " + fName);
        out.flush();

        FileEvent fileEvent = (FileEvent) ois.readObject();
        if (fileEvent instanceof FileModificationEvent) {
            FileModificationEvent fileModificationEvent = (FileModificationEvent) fileEvent;
            File [] fNameList = dir.listFiles((file,name)->name.equals(fName));
            if (fNameList.length!=0) {
                fNameList[0].delete();
            }
            Set<String> IPs = fileModificationEvent.getIps();
            boolean modificationCompleted = false;
            for (String IP : IPs) {
                // Se conceta a otro cliente en IP
                try (Socket clientSocket = new Socket(IP,6666);
                PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());
                //DataInputStream clientIn = new DataInputStream(clientSocket.getInputStream());
                InputStream is = clientSocket.getInputStream();
                Scanner clientIn = new Scanner(is); // Un Scanner para poder leer una línea sin guardar en un buffer más bytes de lo deseado
                FileOutputStream fos = new FileOutputStream(new File(dir,fName));
                ){
                    clientOut.println("GET " + fName);
                    clientOut.flush();

                    String res = clientIn.nextLine();
                    if (res.startsWith("OK")) {
                        byte [] buff = new byte[2048];
                        int read = is.read(buff);
                        while(read!=-1) {
                            fos.write(buff, 0, read);
                            read = is.read(buff, 0, read);
                        }
                        fos.flush();

                        out.println("OK"); // Notifica al servidor de que ha actualizado el archivo
                        modificationCompleted = true;
                        break;
                    } else {
                        //Manejar el error
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Si se ha recorrido IPs entero y no se ha actualizado el archivo, avisar
            if (!modificationCompleted) {
                out.println("ERROR");
            }
            out.flush();
        }
    }
}
