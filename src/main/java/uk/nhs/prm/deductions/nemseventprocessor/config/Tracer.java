package uk.nhs.prm.deductions.nemseventprocessor.config;

import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@NoArgsConstructor
public class Tracer {

    public String startMessageTrace(String originalMessageId) {
        String traceIdUUID = UUID.randomUUID().toString();
        String traceIdHex = traceIdUUID.replaceAll("-", "");
        MDC.put("traceId", traceIdHex);
        MDC.put("meshMessageId", originalMessageId);
        return traceIdHex;
    }

    public String getTraceId() {
        return MDC.get("traceId");
    }

}
