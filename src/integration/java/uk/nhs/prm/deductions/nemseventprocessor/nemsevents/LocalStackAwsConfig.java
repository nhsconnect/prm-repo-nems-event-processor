package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@TestConfiguration
public class LocalStackAwsConfig {

    public final static String UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE = "unhandled_test_receiver";
    public final static String SUSPENSIONS_TEST_RECEIVING_QUEUE = "suspensions_test_receiver";
    public final static String NEMS_EVENTS_AUDIT_TEST_RECEIVING_QUEUE = "nems_events_audit_test_receiver";

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    @Bean
    public static AmazonSQSAsync amazonSQSAsync(@Value("${localstack.url}") String localstackUrl) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("FAKE", "FAKE")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localstackUrl, "eu-west-2"))
                .build();
    }

    @Bean
    public static SnsClient snsClient(@Value("${localstack.url}") String localstackUrl) {
        return SnsClient.builder()
                .endpointOverride(URI.create(localstackUrl))
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
        recreateIncomingNemsQueue();
        CreateTopicResponse topic = snsClient.createTopic(CreateTopicRequest.builder().name("test_unhandled_events_topic").build());
        CreateTopicResponse suspensionsTopic = snsClient.createTopic(CreateTopicRequest.builder().name("test_suspensions_topic").build());
        CreateTopicResponse nemsEventsAuditTopic =
            snsClient.createTopic(CreateTopicRequest.builder().name("test_nems_events_audit_topic").build());
        snsClient.createTopic(CreateTopicRequest.builder().name("test_dead_letter_topic").build());

        createSnsTestReceiverSubscription(topic, UNHANDLED_EVENTS_TEST_RECEIVING_QUEUE);
        createSnsTestReceiverSubscription(suspensionsTopic, SUSPENSIONS_TEST_RECEIVING_QUEUE);
        createSnsTestReceiverSubscription(nemsEventsAuditTopic, NEMS_EVENTS_AUDIT_TEST_RECEIVING_QUEUE);
    }

    private void recreateIncomingNemsQueue() {
        ensureQueueDeleted(nemsEventQueueName);
        createQueue(nemsEventQueueName);
    }

    private void createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.setQueueName(queueName);
        HashMap<String, String> attributes = new HashMap<>();
        createQueueRequest.withAttributes(attributes);
        amazonSQSAsync.createQueue(queueName);
    }

    private void ensureQueueDeleted(String queueName) {
        try {
            deleteQueue(queueName);
        }
        catch (QueueDoesNotExistException e) {
            // no biggie
        }
    }

    private void deleteQueue(String queueName) {
        amazonSQSAsync.deleteQueue(amazonSQSAsync.getQueueUrl(queueName).getQueueUrl());
    }

    private void createSnsTestReceiverSubscription(CreateTopicResponse topic, String queue) {
        String testReceiverQueueArn = createQueueAndGetArn(queue);
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("RawMessageDelivery", "True");
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topic.topicArn())
                .protocol("sqs")
                .endpoint(testReceiverQueueArn)
                .attributes(attributes)
                .build();

        snsClient.subscribe(subscribeRequest);
    }

    private String createQueueAndGetArn(String queue) {
        CreateQueueResult testReceiver = amazonSQSAsync.createQueue(queue);
        GetQueueAttributesResult queueAttributes = amazonSQSAsync.getQueueAttributes(testReceiver.getQueueUrl(), List.of("QueueArn"));
        return queueAttributes.getAttributes().get("QueueArn");
    }
}

