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
import java.util.Calendar;
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
        
        List<String> serverFiles = new ArrayList<>();
        
        Client.log("Ejecutando actualización periódica.");
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

            // Primero leemos todas las líneas del GETALL
            while (res!=null && !res.startsWith("END")) {
            	serverFiles.add(res);
                
                res = in.readLine();
            }
            // Para cada actualización, comprobar si es una modificación o un borrado.
            // Cada vez que hacemos una operación sobre un fichero, la borramos de la 
            // lista files. Así sabremos si hay algún archivo sobre el que no hemos 
            // sido notificados.
            for(String line : serverFiles) {
            	String fName = line.split(" ")[1];
                File [] fNameList = dir.listFiles((file,name)->name.equals(fName));              
                if (line.startsWith("MODIFICATION")) {
                    if (files.contains(fName)) { // En el caso de que ya exista el archivo en la carpeta Cliente
                        long lastModifiedClient = fNameList[0].lastModified();
                        long lastModifiedServer = Long.parseLong(line.split(" ")[2]);
                        if (lastModifiedServer<lastModifiedClient) { // Si el archivo del Cliente es una versión más nueva que la del Servidor
                            Client.log("El archivo " + fName + " es más nuevo que el servidor. Mandando actualización.");
                            notifyModification(fName, lastModifiedClient, in, out, oos);
                        } else if (lastModifiedServer>lastModifiedClient){ // Si el archivo es una versión más vieja que la del Servidor
                            // Hacer un GET y conseguir el archivo
                        	getFileEvent(fName, out, ois, in);
                        }
                        //Si no hemos entrado al if, es porque tenemos la versión más reciente.
                        files.remove(fName);
                    } else { // En el caso de que no exista el archivo en la carpeta Cliente
                        long lastDate = getFile(fName);
                    	if (lastDate == -1L) {
                    		// El archivo no estaba en la carpeta
                    		
                    		// Hacer un GET y conseguir el archivo
                            getFileEvent(fName, out, ois, in);
                    	} else {
                    		// El archivo estaba antes en la carpeta, y ahora ya no!!
                    		// Eso es que lo ha borrado el cliente.
                    		Client.log("Detectado borrado de archivo " + fName + ". Notificando al servidor.");
                    		
                    		//Usamos getTimeInMillis porque asumimos que se ha borrado ahora
                    		//TODO: ESTO NO FUNCIONA PARA RENOMBRAR ARCHIVOS:
                    		//      t=0 crear hola.txt -> t=1 renombrar a hola2.txt -> t=2 renombrar a hola.txt
                    		//      ERROR! hola.txt está borrado en t=1 pero la fecha de modificación es t=0,
                    		//      luego no se actualizará.
                    		//
                    		// Posible solución: hacer que el servidor permita cualquier MODIFY después de un DELETE
                    		// aunque hay que pensarlo bien para no liarla en otros casos.
                    		notifyDelete(fName, Calendar.getInstance().getTimeInMillis(), in, out, oos, ois);
                    		
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
            }
            for (String notUpdatedFile : files) {
            	//Aqui notUpdatedFile es un archivo que no hemos recibido del servidor pero que tenemos
            	File ourFile = dir.listFiles((file,name)->name.equals(notUpdatedFile))[0];
            	Client.log("Añadiendo archivo " + notUpdatedFile + " al servidor.");
            	notifyModification(notUpdatedFile, ourFile.lastModified(), in, out, oos);
            	
            }
            

            //Antes de acabar, actualizamos el registro de archivos
            updateFileRegister();
        } catch (IOException | ClassNotFoundException e) {
            Client.log(e.getLocalizedMessage());
        }
        Client.log("Finalizada actualización periódica.");
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
        Client.log("Server sent after deletion: " + res);
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
