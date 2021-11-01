package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

public class NemsEventMessage {
    public static NemsEventMessage deduction() {
        return new NemsEventMessage(NemsEventType.DEDUCTION);
    }
    public static NemsEventMessage nonDeduction() {
        return new NemsEventMessage(NemsEventType.NON_DEDUCTION);
    }

    /*
    "nhsNumber": "1234567890",
  "previousOdsCode": "A1234",
  "lastUpdated": <ISO 8601 value from meta.lastUpdated>,
  "eventType": "DEDUCTION"
     */
    // private final int nhsNumber;
    private final NemsEventType eventType;

    private NemsEventMessage(NemsEventType eventType) {
        this.eventType = eventType;
    }


    public boolean isDeduction() {
        return this.eventType == NemsEventType.DEDUCTION;
    }
}
