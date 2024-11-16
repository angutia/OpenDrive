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


    /** TODO: NECESITAMOS ESTO?
     * Two files are considered "equal" if their names are the same
     * @param obj The object to be compared with this abstract pathname
     *
     * @return true if this.name==(File) obj.name, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileModificationEvent) {
            return this.getName().equals(((FileModificationEvent) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return "MODIFICATION " + super.toString();
    }
}
