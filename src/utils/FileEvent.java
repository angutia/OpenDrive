package utils;

import java.io.Serializable;

public abstract class FileEvent implements Serializable {

    protected String fileName;
    protected long eventTime;
    public FileEvent(String fileName, long eventTime) {
        this.fileName=fileName;
        this.eventTime=eventTime;
    }

    public String getName() {
        return this.fileName;
    }
    public long getTime() {
        return this.eventTime;
    }

    /**
     * Return a short string representation of this object
     * @return "{evenType} {fileName} {eventTime}"
     */
    public String toString() {
        return this.eventTime + " " + this.fileName;
    }
}
