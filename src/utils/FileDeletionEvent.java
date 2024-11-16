package utils;

public class FileDeletionEvent extends FileEvent{

    public FileDeletionEvent(String fileName, long eventTime) {
        super(fileName, eventTime);
    }

    @Override
    public EventType getType() {
        return EventType.DELETION;
    }
}
