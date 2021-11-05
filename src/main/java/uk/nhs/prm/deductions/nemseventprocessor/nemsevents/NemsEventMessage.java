package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import java.util.HashMap;
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
    private String previousOdsCode;

    private NemsEventMessage(NemsEventType eventType, String nhsNumber, String lastUpdated, String previousOdsCode) {
        this.eventType = eventType;
        this.nhsNumber = nhsNumber;
        this.lastUpdated = lastUpdated;
        this.previousOdsCode = previousOdsCode;
    }

    public boolean isDeduction() {
        return this.eventType == NemsEventType.DEDUCTION;
    }

    @Override
    public Map<String, String> exposeSensitiveData() {
        HashMap<String, String> sensitiveData = new HashMap<>();
        sensitiveData.put("previousOdsCode", this.previousOdsCode);
        sensitiveData.put("nhsNumber", this.nhsNumber);
        sensitiveData.put("lastUpdated", this.lastUpdated);
        sensitiveData.put("eventType", this.eventType.toString());
        return sensitiveData;
    }
}
