package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public class NemsEventParseException extends RuntimeException {
    public NemsEventParseException(RuntimeException cause) {
        super(cause);
    }
}
