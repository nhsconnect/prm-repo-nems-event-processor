package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public class NemsEventParseException extends RuntimeException {
    public NemsEventParseException(Exception cause) {
        super(cause);
    }

    public NemsEventParseException(String message) {
        super(NemsEventParseException.class.getSimpleName() + ": " + message);
    }
}
