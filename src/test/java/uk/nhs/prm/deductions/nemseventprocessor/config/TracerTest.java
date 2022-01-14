package uk.nhs.prm.deductions.nemseventprocessor.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.prm.deductions.nemseventprocessor.config.Tracer.MESH_MESSAGE_ID;
import static uk.nhs.prm.deductions.nemseventprocessor.config.Tracer.NEMS_MESSAGE_ID;
import static uk.nhs.prm.deductions.nemseventprocessor.config.Tracer.TRACE_ID;

class TracerTest {

    private static final String SOME_NEMS_ID = "someNemsId";


    @Test
    void startTraceShoulClearOldValuesAndSetMeshIdAndTraceId() {
        MDC.put(NEMS_MESSAGE_ID, "randomNemsId");
        MDC.put(TRACE_ID, "randomTracecId");
        MDC.put(MESH_MESSAGE_ID, "randomMeshId");

        Tracer tracer = new Tracer();
        tracer.startMessageTrace("someMeshId");

        assertThat(MDC.get(MESH_MESSAGE_ID)).isEqualTo("someMeshId");
        assertThat(MDC.get(TRACE_ID)).isNotNull();
        assertThat(MDC.get(TRACE_ID)).isNotEqualTo("randomTracecId");
        assertThat(MDC.get(NEMS_MESSAGE_ID)).isNull();
    }

    @Test
    void shouldAddNemsMessageIdToMDC() {
        Tracer tracer = new Tracer();
        tracer.setNemsMessageId(SOME_NEMS_ID);
        String mdcValue = MDC.get(NEMS_MESSAGE_ID);
        assertThat(mdcValue).isEqualTo(SOME_NEMS_ID);
    }

    @Test
    void shouldGetNemsMessageIdFromMDC() {
        MDC.put(NEMS_MESSAGE_ID, SOME_NEMS_ID);
        Tracer tracer = new Tracer();
        String nemsMessageIdFromTracer = tracer.getNemsMessageId();
        assertThat(nemsMessageIdFromTracer).isEqualTo(SOME_NEMS_ID);
    }

    @Test
    void shouldGetTraceIdFromMDC() {
        MDC.put(TRACE_ID, "someTrace");
        Tracer tracer = new Tracer();
        String traceId = tracer.getTraceId();
        assertThat(traceId).isEqualTo("someTrace");
    }
}
