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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (!dir.isDirectory()) {
        	Client.log("Imposible acceder al directorio en la ruta especificada.");
        	return;
        }
        List<String> currentFiles = new ArrayList<>(Arrays.asList(dir.list()));
        
        List<String> serverFiles = new ArrayList<>();
        
        Client.log("Ejecutando actualización periódica.");
        // Lo primero es pedir al servidor la última actualización de todos los archivos de la carpeta
        try (Socket socket = new Socket(Client.getServerHost(), Client.getServerPort());
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        // El orden importa
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
            	String fName = line.substring(line.indexOf(" ", line.indexOf(" ")+1)+1);
                File [] fNameList = dir.listFiles((file,name)->name.equals(fName));              
                if (line.startsWith("MODIFICATION")) {
                    if (currentFiles.contains(fName)) { // En el caso de que ya exista el archivo en la carpeta Cliente
                        long lastModifiedClient = fNameList[0].lastModified();
                        long lastModifiedServer = Long.parseLong(line.split(" ")[1]);
                        if (lastModifiedServer<lastModifiedClient) { // Si el archivo del Cliente es una versión más nueva que la del Servidor
                            Client.log("El archivo " + fName + " es más nuevo que en el servidor. Mandando actualización.");
                            notifyModification(fName, lastModifiedClient, os, is, oos);
                        } else if (lastModifiedServer>lastModifiedClient){ // Si el archivo es una versión más vieja que la del Servidor
                            // Hacer un GET y conseguir el archivo
                            Client.log("El archivo " + fName + " es más antiguo que en el servidor. Obteniendo actualización.");
                        	getFileEvent(fName, os, is, ois);
                        }
                        //Si no hemos entrado al if, es porque tenemos la versión más reciente.
                        currentFiles.remove(fName);
                    } else { // En el caso de que no exista el archivo en la carpeta Cliente
                        long lastDate = getFile(fName);
                    	if (lastDate == -1L) {
                    		// El archivo no estaba en la carpeta
                    		Client.log("El archivo " + fName + " es nuevo. Obteniendo archivo.");
                    		// Hacer un GET y conseguir el archivo
                            getFileEvent(fName, os, is, ois);
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
                    		notifyDelete(fName, Calendar.getInstance().getTimeInMillis(), os, is, oos);
                    	}
                    }                    
                } else if (line.startsWith("DELETION")) {
                    if (currentFiles.remove(fName)) { // En el caso de que exista en la carpeta Cliente el archivo que ha sido borrado
                        // Manejar el borrado
                        Client.log("Borrando el archivo " + fName);
                        fNameList[0].delete();
                    } else {
                        // En principio nada, el archivo ha sido borrado pero no estaba en la carpeta del cliente así que se queda igual
                    } 
                }
            }
            for (String notUpdatedFile : currentFiles) {
            	//Aqui notUpdatedFile es un archivo que no hemos recibido del servidor pero que tenemos
            	File ourFile = dir.listFiles((file,name)->name.equals(notUpdatedFile))[0];
            	Client.log("Añadiendo archivo " + notUpdatedFile + " al servidor.");
            	notifyModification(notUpdatedFile, ourFile.lastModified(), os, is, oos);
            }
            

            //Antes de acabar, actualizamos el registro de archivos
            updateFileRegister();
        } catch (IOException | ClassNotFoundException e) {
            Client.log(e.getLocalizedMessage());
        }
        Client.log("Finalizada actualización periódica.");
    }

    private void notifyModification (String fileName, long lastModified, OutputStream os, InputStream is, ObjectOutputStream oos) throws IOException {
        PrintWriter out = new PrintWriter(os);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
        FileEvent fileModification = new FileModificationEvent(fileName, lastModified);
        out.println("PUSH");
        out.flush();
        oos.writeObject(fileModification);
        oos.flush();

        String res = in.readLine();
        if (res.startsWith("ERROR")) {
            Client.log("Error al notificar al servidor de la modificación del archivo " + fileName + ": " + res);
        } else {
            Client.log("Servidor notificado de la modificación del archivo " + fileName);
        }
    }

    private void notifyDelete (String fileName, long lastModified, OutputStream os, InputStream is, ObjectOutputStream oos) throws IOException {
        PrintWriter out = new PrintWriter(os);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
        FileEvent fileModification = new FileDeletionEvent(fileName, lastModified);
        out.println("PUSH");
        out.flush();
        oos.writeObject(fileModification);
        oos.flush();

        String res = in.readLine();
        if (res.startsWith("ERROR")) {
            Client.log("Error al notificar al servidor del borrado del archivo " + fileName + ": " + res);
        } else {
            Client.log("Servidor notificado del borrado del archivo " + fileName);
        }
    }

    private void getFileEvent (String fName, OutputStream os, InputStream is, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        PrintWriter out = new PrintWriter(os);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
        out.println("GET " + fName);
        out.flush();
        String readOk = in.readLine();
        if (!readOk.equals("OK")) {
            Client.log("Error al obtener del servidor la modificación del archivo " + fName + ": " + readOk);
        	return;
        }

        FileEvent fileEvent = (FileEvent) ois.readObject();
        // ¿En qué caso no sería un modification event?
        // Porque siempre que se llama a este método es desde una modificación del getall
        if (!(fileEvent instanceof FileModificationEvent)) {
            Client.log("El archivo ha sido borrado del servidor");
            return;
        }
        
        FileModificationEvent fileModificationEvent = (FileModificationEvent) fileEvent;
        File [] fNameList = dir.listFiles((file,name)->name.equals(fName));
        
        Set<String> IPs = fileModificationEvent.getIps();
        boolean modificationCompleted = false;
        File toCreate = new File(dir, fName);
        for (String IP : IPs) {
        	if (modificationCompleted) break;
            // Se conecta a otro cliente en IP
            try (Socket clientSocket = new Socket(IP,6666);
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());
            InputStream clientIs = clientSocket.getInputStream();
            DataInputStream clientIn = new DataInputStream(clientIs); 
            FileOutputStream fos = new FileOutputStream(toCreate);
            ){
                clientOut.println("GET " + fName);
                clientOut.flush();

                @SuppressWarnings("deprecation")
				String res = clientIn.readLine();
                if (res.startsWith("OK")) {
                	if (fNameList.length!=0) {
                        fNameList[0].delete(); // Solo borramos el archivo si sabemos que vamos a poder actualizarlo
                    }
                    byte [] buff = new byte[2048];
                    int read = clientIs.read(buff);
                    while(read!=-1) {
                        fos.write(buff, 0, read);
                        read = clientIs.read(buff, 0, read);
                    }
                    fos.flush();

                    Client.log("Archivo " + fName + " actualizado correctamente desde el cliente " + IP);
                    out.println("OK"); // Notifica al servidor de que ha actualizado el archivo
                    modificationCompleted = true;
                    //Ponemos la fecha de útlima modificación correcta
                    if (!toCreate.setLastModified(fileModificationEvent.getTime()))
                    	Client.log("Error al establecer la fecha de modificación del archivo. Podrían perderse datos.");
                } else {
                    Client.log("Error al obtener el archivo " + fName + " del cliente " + IP + ": " + res);
                }
                
            } catch (IOException e) {
                Client.log(e.getLocalizedMessage());
            }
        }
        // Si se ha recorrido IPs entero y no se ha actualizado el archivo, avisar
        if (!modificationCompleted) {
            Client.log("Imposible obtener el archivo " + fName + " de ninguno de los clientes proporcionados por el servidor.");
            out.println("ERROR");
        }
        out.flush();
        
    }
}
