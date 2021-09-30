package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Component
@Slf4j
public class HealthMetricPublisher {

    private static final int SECONDS = 1000;
    private static final int MINUTE_INTERVAL = 10 * SECONDS;
    private CloudWatchClient cloudWatchClient;

    @Autowired
    public HealthMetricPublisher(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    @Scheduled(fixedRate = MINUTE_INTERVAL)
    public void publishHealthyStatus() {
        log.info("SCHEDULED");
        ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(Dimension
                .builder()
                .name("Environment")
                .value("ci")
                .build());

        MetricDatum datum = MetricDatum
                .builder()
                .metricName("Health")
                .value(1.0)
                .timestamp(awsCompatibleNow())
                .dimensions(dimensions)
                .build();

        PutMetricDataRequest request =
                PutMetricDataRequest
                        .builder()
                        .namespace("PrmDeductions/NemsEventProcessor")
                        .metricData(datum)
                        .build();

        cloudWatchClient.putMetricData(request);
    }

    // why? here's why: https://forums.aws.amazon.com/thread.jspa?threadID=328321
    private Instant awsCompatibleNow() {
        return Instant.parse(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT));
    }
}
