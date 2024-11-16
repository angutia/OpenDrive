package utils;

public abstract class FileEvent {
    enum EventType {
        MODIFICATION,
        DELETION
    }
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

    public abstract EventType getType();
}
