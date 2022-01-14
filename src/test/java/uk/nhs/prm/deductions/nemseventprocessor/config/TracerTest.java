package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.deductions.nemseventprocessor.config.Tracer.MESH_MESSAGE_ID;
import static uk.nhs.prm.deductions.nemseventprocessor.config.Tracer.TRACE_ID;

class TracerTest {

    private static final String NEMS_MESSAGE_ID = "someNemsId";


    @Test
    void startTraceShouldSetRandomTraceIdAndSetMeshMessageId() {
        Tracer tracer = new Tracer();
        tracer.startMessageTrace("someMeshId");

        assertThat(MDC.get(MESH_MESSAGE_ID)).isEqualTo("someMeshId");
        assertThat(MDC.get(TRACE_ID)).isNotNull();
    }

    @Test
    void shouldAddNemsMessageIdToMDC() {
        Tracer tracer = new Tracer();
        tracer.setNemsMessageId(NEMS_MESSAGE_ID);
        String mdcValue = MDC.get(Tracer.NEMS_MESSAGE_ID);
        assertThat(mdcValue).isEqualTo(NEMS_MESSAGE_ID);
    }

    @Test
    void shouldGetNemsMessageIdFromMDC() {
        MDC.put(Tracer.NEMS_MESSAGE_ID, NEMS_MESSAGE_ID);
        Tracer tracer = new Tracer();
        String nemsMessageIdFromTracer = tracer.getNemsMessageId();
        assertThat(nemsMessageIdFromTracer).isEqualTo(NEMS_MESSAGE_ID);
    }

    @Test
    void shouldGetTraceIdFromMDC() {
        MDC.put(TRACE_ID, "someTrace");
        Tracer tracer = new Tracer();
        String traceId = tracer.getTraceId();
        assertThat(traceId).isEqualTo("someTrace");
    }
}
