package server;

import utils.FileEvent;
import utils.FileModificationEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects and stores file uploads
 * TODO: DELETE A FILE
 */
public class UpdateLog {

    final Set<FileEvent> log; //Archivo, fechaultimamodificacion

    public UpdateLog() {
        this.log = new HashSet<>();
    }

    /**
     * Tries to push an update to the log. If time is newer than the last modified time, pushes the update and
     * returns time. If it is older, returns the newest time.
     * @param newEvent the new event sent by the server
     * @return the time of last modification of the file
     */
    public long pushUpdate(FileEvent newEvent, String ip) {
        FileEvent file;
        synchronized (this.log) {
            file = this.getFileEventByName(newEvent.getName());
            if (file != null) {
                if (file.getTime()> newEvent.getTime()) {
                    //File is newer, we don't add the sender ip
                    return file.getTime();
                } else {
                    //newEvent is newer
                    this.log.remove(file);
                }
            }
            //In this case, file is not in log OR newEvent is newer and file has been removed from the log
            //Add it to the log
            this.log.add(newEvent);
            //if it is a modification, add the ip to the list
            if (newEvent instanceof FileModificationEvent) ((FileModificationEvent) newEvent).addIp(ip);
            return newEvent.getTime();

        }

    }
    //Add an IP to a file, assuming the file is a modification
    public void addIP(String filename, String ip) {
        FileEvent e = this.getFileEventByName(filename);
        if (e instanceof FileModificationEvent event) {
            event.addIp(ip);
        }
    }

    /**
     * Returns a list of files that were updated after the timestamp
     * @return a list of the latest FileEvent.
     */
    public List<FileEvent> getFiles() {
        synchronized (this.log) {
            return List.copyOf(this.log);
        }
    }

    //Returns null if no file found
    public FileEvent getFileEventByName(String filename) {
        synchronized (this.log) {
            return this.log.stream().filter(f -> f.getName().equals(filename)).findFirst().orElse(null);
        }
    }


}
