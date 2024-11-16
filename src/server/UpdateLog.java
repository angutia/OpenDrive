package server;

import utils.FileModificationEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Collects and stores file uploads
 * TODO: DELETE A FILE
 */
public class UpdateLog {

    final Set<FileModificationEvent> log; //Archivo, fechaultimamodificacion

    public UpdateLog() {
        this.log = new HashSet<>();
    }

    /**
     * Tries to push an update to the log. If time is newer than the last modified time, pushes the update and
     * returns time. If it is older, returns the newest time.
     * @param time The unix time of the modification
     * @param filename The name of the file being modified
     * @param ip The ip of the pusher
     * @return the time of last modification of the file
     */
    public long pushUpdate(long time, String filename, String ip) {
        FileModificationEvent file = null;
        synchronized (this.log) {
            file = this.getFileByName(filename);
            if (file != null) {
                if (file.getTime() < time) {
                    file.newTime(time);
                    file.addIp(ip);
                    return time;
                } else {
                    return file.getTime();
                }
            } else {
                file = new FileModificationEvent(filename, ip, time);
                this.log.add(file);
                return time;
            }
        }

    }

    /**
     * Returns a list of files that were updated after the timestamp
     * @param timestamp the timestamp
     * @return a list of all files for which File.getTime()>timestamp.
     */
    public List<FileModificationEvent> getFilesNewerThan(long timestamp) {
        synchronized (this.log) {
            return this.log.stream().filter(f -> f.getTime()>timestamp).collect(Collectors.toList());
        }
    }

    //Returns null if no file found
    public FileModificationEvent getFileByName(String filename) {
        synchronized (this.log) {
            return this.log.stream().filter(f -> f.getName().equals(filename)).findFirst().orElse(null);
        }
    }


}
