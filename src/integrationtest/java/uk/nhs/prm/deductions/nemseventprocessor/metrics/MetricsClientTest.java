package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetricsClientTest {

    CloudWatchClient cloudWatchClient = CloudWatchClient.create();

    @Test
    void shouldPutHealthMetricDataIntoCloudWatch() {
        HealthMetricPublisher publisher = new HealthMetricPublisher(cloudWatchClient);

        publisher.publishHealthyStatus();
        
        List<Metric> metrics = fetchMetricsMatching("PrmDeductions/NemsEventProcessor", "Health");
        assertThat(metrics).isNotEmpty();
        assertThat(filterForMetricsMatchingDimension(metrics, "Environment", "ci")).isNotEmpty();
    }

    private List<Metric> filterForMetricsMatchingDimension(List<Metric> metrics, String dimensionName, String dimensionValue) {
        return metrics.stream().filter(metric -> metric.dimensions().stream().anyMatch(dimension ->
                dimension.name().equals(dimensionName) && dimension.value().equals(dimensionValue))).collect(Collectors.toList());
    }

    private List<Metric> fetchMetricsMatching(String namespace, String metricName) {
        ListMetricsRequest request = ListMetricsRequest.builder()
                .namespace(namespace)
                .metricName(metricName)
                .build();

        ListMetricsResponse listMetricsResponse = cloudWatchClient.listMetrics(request);
        return listMetricsResponse.metrics();
    }
}