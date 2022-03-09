package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public class StubbedNemsEventService implements NemsEventHandler {

    private volatile boolean processed = false;

    public void throwOnProcessEvent() {
    }

    @Override
    public void processNemsEvent(String message) {
        processed = true;
        throw new RuntimeException("boom from " + getClass());
    }

    public void waitUntilProcessed() {
        do {
            waitABit();
        } while (!processed);
        waitABit();
    }

    private void waitABit() {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            // nop
        }
    }
}
