package server.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Collects and stores file uploads
 * TODO: DELETE A FILE
 */
public class UpdateLog {

    final Set<File> log; //Archivo, fechaultimamodificacion

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
        File file = null;
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
                file = new File(filename, ip, time);
                this.log.add(file);
                return time;
            }
        }

    }

    public String getFileString(String filename) {
        StringBuilder builder = new StringBuilder();
        File f = null;
        Set<String> ips;
        long time ;
        synchronized (this.log) {
            f = this.getFileByName(filename);
            if (f==null) return "";
            ips = f.getIps();
            time = f.getTime();
        }
        builder.append(filename);
        builder.append(" ");
        builder.append(time);
        builder.append(" ");
        for (String ip : ips) {
            builder.append(ip);
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1); //Remove last ','
        return builder.toString();

    }

    //Returns null if no file found
    private File getFileByName(String filename) {
        return this.log.stream().filter(f -> f.getName().equals(filename)).findFirst().orElse(null);
    }
}
