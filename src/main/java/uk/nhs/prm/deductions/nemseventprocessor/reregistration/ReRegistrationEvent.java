package uk.nhs.prm.deductions.nemseventprocessor.reregistration;

import com.google.gson.GsonBuilder;
import lombok.Data;

@Data
public class ReRegistrationEvent {
    private final String nhsNumber;
    private final String newlyRegisteredOdsCode;
    private final String nemsMessageId;
    private final String lastUpdated;

    public ReRegistrationEvent(String nhsNumber, String newlyRegisteredOdsCode, String nemsMessageId, String lastUpdated) {
        this.nhsNumber = nhsNumber;
        this.newlyRegisteredOdsCode = newlyRegisteredOdsCode;
        this.nemsMessageId = nemsMessageId;
        this.lastUpdated = lastUpdated;
    }

    public String toJsonString() {
        return new GsonBuilder().disableHtmlEscaping().create()
                .toJson(this);
    }
}
