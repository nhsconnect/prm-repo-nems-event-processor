package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import uk.nhs.prm.deductions.nemseventprocessor.config.AppConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthMetricPublisherTest {

    @Captor
    private ArgumentCaptor<PutMetricDataRequest> putRequestCaptor;
    @Value("${environment}")
    String environment;


    @Test
    public void shouldSetHealthMetricDimensionToAppropriateEnvironment() {
        AppConfig config = Mockito.mock(AppConfig.class);
        CloudWatchClient metricsClient = Mockito.mock(CloudWatchClient.class);

        when(config.environment()).thenReturn("performance");

        new HealthMetricPublisher(metricsClient, config).publishHealthyStatus();

        verify(metricsClient).putMetricData(putRequestCaptor.capture());
        Dimension environmentDimension = putRequestCaptor.getValue().metricData().get(0).dimensions().get(0);

        assertThat(environmentDimension.name()).isEqualTo("Environment");
        assertThat(environmentDimension.value()).isEqualTo("performance");
    }

}
