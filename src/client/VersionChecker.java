package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimerTask;
import utils.FileDeletionEvent;
import utils.FileEvent;
import utils.FileModificationEvent;

public class VersionChecker extends TimerTask{
    private String dirRoute;
    private File dir;
    private static Map<String,Long> fileRegister = new HashMap<>();

    public VersionChecker(String dir) {
        this.dirRoute = dir;
    }

    public synchronized void updateFileRegister () {
        for (File file : dir.listFiles()){
            fileRegister.put(file.getName(), file.lastModified());
        }
    }
    
    public synchronized long getFile(String name) {
    	return fileRegister.getOrDefault(name, -1L);
    }

    @Override
    public void run() {
        dir = new File(dirRoute);
        List<String> files = new ArrayList<>(Arrays.asList(dir.list()));
        
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
                        long lastDate = getFile(fName);
                    	if (lastDate == -1L) {
                    		// El archivo no estaba en la carpeta
                    		
                    		// Hacer un GET y conseguir el archivo
                            files.add(fName);
                    	} else {
                    		// El archivo estaba antes en la carpeta, y ahora ya no!!
                    		// Eso es que lo ha borrado el cliente.
                    		
                    		//TODO: notificar el borrado despues del GETALL
                    		notifyDelete(fName, lastDate, in, out, oos, ois);
                    		
                    	}
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
                getFileEvent(fName, out, ois, in);
            }


            //Antes de acabar, actualizamos el registro de archivos
            updateFileRegister();
        } catch (IOException | ClassNotFoundException e) {
            Client.log(e.getLocalizedMessage());
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

    private void getFileEvent (String fName, PrintWriter out, ObjectInputStream ois, BufferedReader reader) throws IOException, ClassNotFoundException {
        out.println("GET " + fName);
        out.flush();
        String readOk = reader.readLine();
        if (!readOk.equals("OK")) {
        	Client.log("Server sent error: " + readOk);
        	return;
        }

        FileEvent fileEvent = (FileEvent) ois.readObject();
        if (!(fileEvent instanceof FileModificationEvent)) return;
        
        FileModificationEvent fileModificationEvent = (FileModificationEvent) fileEvent;
        File [] fNameList = dir.listFiles((file,name)->name.equals(fName));
        if (fNameList.length!=0) {
            fNameList[0].delete();
        }
        Set<String> IPs = fileModificationEvent.getIps();
        boolean modificationCompleted = false;
        for (String IP : IPs) {
        	if (modificationCompleted) break;
            // Se conecta a otro cliente en IP
            try (Socket clientSocket = new Socket(IP,6666);
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());
            //DataInputStream clientIn = new DataInputStream(clientSocket.getInputStream());
            InputStream is = clientSocket.getInputStream();
            DataInputStream clientIn = new DataInputStream(is); 
            FileOutputStream fos = new FileOutputStream(new File(dir,fName));
            ){
                clientOut.println("GET " + fName);
                clientOut.flush();

                @SuppressWarnings("deprecation")
				String res = clientIn.readLine();
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
