package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.joda.time.DateTime;

import java.util.Map;

interface HasSensitiveData {
    public Map<String, String> exposeSensitiveData();
}

public class NemsEventMessage implements HasSensitiveData {
    public static NemsEventMessage deduction(String nhsNumber, String lastUpdated, String odsCode) {
        return new NemsEventMessage(NemsEventType.DEDUCTION, nhsNumber, lastUpdated, odsCode);
    }

    public static NemsEventMessage nonDeduction() {
        return new NemsEventMessage(NemsEventType.NON_DEDUCTION, null, null, null);
    }

    private final NemsEventType eventType;
    private String nhsNumber;
    private String lastUpdated;
    private String odsCode;

    private NemsEventMessage(NemsEventType eventType, String nhsNumber, String lastUpdated, String odsCode) {
        this.eventType = eventType;
        this.nhsNumber = nhsNumber;
        this.lastUpdated = lastUpdated;
        this.odsCode = odsCode;
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

    public String getPreviousOdsCode() {
        return this.odsCode;
    }

    @Override
    public Map<String, String> exposeSensitiveData() {
        return null;
    }
}
