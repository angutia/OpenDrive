package utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a File stored in the shared folder
 */
public class FileModificationEvent extends FileEvent{
    private HashSet<String> ips; //Set of ips containing this file version

    public FileModificationEvent(String pathname, long firstTime) {
        super(pathname, firstTime);
        this.ips = new HashSet<>();
    }

    public boolean addIp(String ip) {
        return this.ips.add(ip);
    }

    public Set<String> getIps() {
        return this.ips;
    }

    public void newTime(long time) {
        this.ips.clear();
        this.eventTime=time;
    }



    @Override
    public String toString() {
        return "MODIFICATION " + super.toString();
    }
}
