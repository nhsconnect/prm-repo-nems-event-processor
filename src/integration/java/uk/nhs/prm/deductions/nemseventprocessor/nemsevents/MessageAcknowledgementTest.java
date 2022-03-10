package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        LocalStackAwsConfig.class
})
class MessageAcknowledgementTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @MockBean
    private NemsEventHandler nemsEventHandler;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    private StubbedNemsEventHandler stubbedNemsEventHandler;

    @BeforeEach
    public void setUpStubbedHandler() {
        stubbedNemsEventHandler = new StubbedNemsEventHandler();
        doAnswer(invocation -> {
            stubbedNemsEventHandler.processNemsEvent(invocation.getArgument(0));
            return null;
        }).when(nemsEventHandler).processNemsEvent(anyString());
    }

    @Test
    void shouldNotImplicitlyAcknowledgeAFailedMessageWhenTheNextMessageIsProcessedOk_SoThatItIsThereToBeReprocessedAfterVisibilityTimeout() {
        String queueUrl = amazonSQSAsync.getQueueUrl(nemsEventQueueName).getQueueUrl();

        amazonSQSAsync.sendMessage(queueUrl, "throw me");
        stubbedNemsEventHandler.waitUntilProcessed("throw me", 10);

        amazonSQSAsync.sendMessage(queueUrl, "process me ok");
        stubbedNemsEventHandler.waitUntilProcessed("process me ok", 10);

        assertThat(getIncomingNemsMessagesCount("ApproximateNumberOfMessagesNotVisible")).isEqualTo(1);
    }

    private int getIncomingNemsMessagesCount(String countAttributeName) {
        var incomingQueue = amazonSQSAsync.getQueueUrl(nemsEventQueueName).getQueueUrl();
        var attributes = amazonSQSAsync.getQueueAttributes(new GetQueueAttributesRequest()
                .withAttributeNames(countAttributeName)
                .withQueueUrl(incomingQueue)).getAttributes();
        return Integer.parseInt(attributes.get(countAttributeName));
    }
}
