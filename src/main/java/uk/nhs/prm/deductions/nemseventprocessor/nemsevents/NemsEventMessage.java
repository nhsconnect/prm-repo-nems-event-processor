package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

interface HasSensitiveData {
    public Map<String, String> exposeSensitiveData();
}

public class NemsEventMessage implements HasSensitiveData {
    private final NemsEventType eventType;
    private final String nhsNumber;
    private final String lastUpdated;
    private final String previousOdsCode;
    private final String nemsMessageId;

    public static NemsEventMessage suspension(String nhsNumber, String lastUpdated, String odsCode, String nemsMessageId) {
        return new NemsEventMessage(NemsEventType.SUSPENSION, nhsNumber, lastUpdated, odsCode, nemsMessageId);
    }

    public static NemsEventMessage nonSuspension(String nemsMessageId) {
        return new NemsEventMessage(NemsEventType.NON_SUSPENSION, null, null, null, nemsMessageId);
    }

    public boolean isSuspension() {
        return this.eventType == NemsEventType.SUSPENSION;
    }

    private NemsEventMessage(NemsEventType eventType, String nhsNumber, String lastUpdated, String previousOdsCode, String nemsMessageId) {
        this.eventType = eventType;
        this.nhsNumber = nhsNumber;
        this.lastUpdated = lastUpdated;
        this.previousOdsCode = previousOdsCode;
        this.nemsMessageId = nemsMessageId;
    }

    @Override
    public Map<String, String> exposeSensitiveData() {
        HashMap<String, String> sensitiveData = new HashMap<>();
        sensitiveData.put("previousOdsCode", this.previousOdsCode);
        sensitiveData.put("nhsNumber", this.nhsNumber);
        sensitiveData.put("lastUpdated", this.lastUpdated);
        sensitiveData.put("eventType", this.eventType.toString());
        sensitiveData.put("nemsMessageId", this.nemsMessageId);
        return sensitiveData;
    }

    public String toJsonString() {
        return new Gson().toJson(this.exposeSensitiveData());
    }
}
