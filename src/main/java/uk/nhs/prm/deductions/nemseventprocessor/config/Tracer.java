package uk.nhs.prm.deductions.nemseventprocessor.config;

import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@NoArgsConstructor
public class Tracer {

    public String createTraceId() {
        String traceIdUUID = UUID.randomUUID().toString();
        String traceIdHex = traceIdUUID.replaceAll("-", "");
        return traceIdHex;
    }

    public void setTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    public String getTraceId() {
        return MDC.get("traceId");
    }
}
