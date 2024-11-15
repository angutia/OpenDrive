package server.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a File stored in the shared folder
 */
public class File {
    private long lastModified;
    private String filename;
    private HashSet<String> ips; //Set of ips containing this file version
    public File(String pathname, String ip) {
        this.ips = new HashSet<>();
        this.filename=pathname;
        this.ips.add(ip);
    }

    public File(String pathname, String ip, long firstTime) {
        this(pathname, ip);
        this.lastModified=firstTime;
    }

    public boolean addIp(String ip) {
        return this.ips.add(ip);
    }

    public Set<String> getIps() {
        return this.ips;
    }

    public void newTime(long time) {
        this.ips.clear();
        this.lastModified=time;
    }

    public long getTime() {
        return this.lastModified;
    }

    public String getName() {
        return this.filename;
    }

    /** TODO: NECESITAMOS ESTO?
     * Two files are considered "equal" if their names are the same
     * @param obj The object to be compared with this abstract pathname
     *
     * @return true if this.name==(File) obj.name, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof File) {
            return this.getName().equals(((File) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        Set<String> ips = this.getIps();
        long time = this.getTime();

        builder.append(this.getName());
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
}
