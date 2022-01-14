package uk.nhs.prm.deductions.nemseventprocessor.audit;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditMessage {
    private String nemsMessageId;
    private String messageBody;

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
