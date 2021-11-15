package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {
    private final String environment;
    private final String incomingQueueName;

    public AppConfig(@Value("${environment}") String environment, @Value("${nemsEventsQueueName}") String incomingQueueName) {
        this.environment = environment;
        this.incomingQueueName = incomingQueueName;
    }

    public String environment() {
        return environment;
    }

    public String incomingQueueName() {
        return incomingQueueName;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}
