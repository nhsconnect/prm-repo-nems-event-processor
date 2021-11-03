package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public class NemsEventMessage {
    public static NemsEventMessage deduction(String nhsNumber) {
        return new NemsEventMessage(NemsEventType.DEDUCTION, nhsNumber);
    }

    public static NemsEventMessage nonDeduction() {
        return new NemsEventMessage(NemsEventType.NON_DEDUCTION);
    }

    private final NemsEventType eventType;
    private String nhsNumber;

    private NemsEventMessage(NemsEventType eventType) {
        this.eventType = eventType;
    }

    public NemsEventMessage(NemsEventType eventType, String nhsNumber) {
        this.eventType = eventType;
        this.nhsNumber = nhsNumber;
    }

    public boolean isDeduction() {
        return this.eventType == NemsEventType.DEDUCTION;
    }

    public String getNhsNumber() {
        return this.nhsNumber;
    }
}
