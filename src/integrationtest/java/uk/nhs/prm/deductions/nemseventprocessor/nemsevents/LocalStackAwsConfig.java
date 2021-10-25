package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;


@TestConfiguration
public class LocalStackAwsConfig {

    public final static String UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE = "unhandled_test_receiver";

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localstack:4566", "eu-west-2"))
            .build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
            .endpointOverride(URI.create("http://localstack:4566"))
            .region(Region.EU_WEST_2)
            .credentialsProvider(StaticCredentialsProvider.create(new AwsCredentials() {
                @Override
                public String accessKeyId() {
                    return "FAKE";
                }

                @Override
                public String secretAccessKey() {
                    return "FAKE";
                }
            }))
            .build();
    }

    @PostConstruct
    public void setupTestQueuesAndTopics() {
        amazonSQSAsync.createQueue(nemsEventQueueName);
        CreateTopicResponse topic = snsClient.createTopic(CreateTopicRequest.builder().name("test_unhandled_events_topic").build());

        createSnsTestReceiverSubscription(topic, UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE);
    }

    private void createSnsTestReceiverSubscription(CreateTopicResponse topic, String queue) {
        String testReceiverQueueArn = createQueueAndGetArn(queue);
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
            .topicArn(topic.topicArn())
            .protocol("sqs")
            .endpoint(testReceiverQueueArn)
            .build();

        snsClient.subscribe(subscribeRequest);
    }

    private String createQueueAndGetArn(String queue) {
        CreateQueueResult testReceiver = amazonSQSAsync.createQueue(queue);
        GetQueueAttributesResult queueAttributes = amazonSQSAsync.getQueueAttributes(testReceiver.getQueueUrl(), List.of("QueueArn"));
        return queueAttributes.getAttributes().get("QueueArn");
    }
}

