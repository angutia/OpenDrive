package utils;

public class FileDeletionEvent extends FileEvent{

    public FileDeletionEvent(String fileName, long eventTime) {
        super(fileName, eventTime);
    }

    @Override
    public String toString() {
        return "DELETION " + super.toString();
    }
}
