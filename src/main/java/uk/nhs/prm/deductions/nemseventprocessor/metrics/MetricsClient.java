package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

public class MetricsClient {

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient
                .builder()
                .region(Region.EU_WEST_2)
                .build();
    }
}
