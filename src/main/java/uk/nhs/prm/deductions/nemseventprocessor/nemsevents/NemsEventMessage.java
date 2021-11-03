package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.joda.time.DateTime;

public class NemsEventMessage {
    public static NemsEventMessage deduction(String nhsNumber, String lastUpdated) {
        return new NemsEventMessage(NemsEventType.DEDUCTION, nhsNumber, lastUpdated);
    }

    public static NemsEventMessage nonDeduction() {
        return new NemsEventMessage(NemsEventType.NON_DEDUCTION, null, null);
    }

    private final NemsEventType eventType;
    private String nhsNumber;
    private String lastUpdated;

    private NemsEventMessage(NemsEventType eventType, String nhsNumber, String lastUpdated) {
        this.eventType = eventType;
        this.nhsNumber = nhsNumber;
        this.lastUpdated = lastUpdated;
    }

    public boolean isDeduction() {
        return this.eventType == NemsEventType.DEDUCTION;
    }

    public String getNhsNumber() {
        return this.nhsNumber;
    }

    public DateTime getLastUpdated() {
        return new DateTime(lastUpdated);
    }
}
