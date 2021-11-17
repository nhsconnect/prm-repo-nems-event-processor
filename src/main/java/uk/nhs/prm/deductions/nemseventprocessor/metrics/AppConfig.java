package uk.nhs.prm.deductions.nemseventprocessor.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class AppConfig {
    private final String environment;
    private final String incomingQueueName;
    private final String unhandledEventsSnsTopicArn;

    public AppConfig(@Value("${environment}") String environment, @Value("${aws.nemsEventsQueueName}") String incomingQueueName, @Value("${aws.unhandledEventsSnsTopicArn}") String unhandledEventsSnsTopicArn) {
        this.environment = environment;
        this.incomingQueueName = incomingQueueName;
        this.unhandledEventsSnsTopicArn = unhandledEventsSnsTopicArn;
    }

    public String environment() {
        return environment;
    }

    public String incomingQueueName() {
        return incomingQueueName;
    }

    public String unhandledEventsSnsTopicArn() {
        return unhandledEventsSnsTopicArn;
    }

    @Bean
    @SuppressWarnings("unused")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.create();
    }
}
