package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        LocalStackAwsConfig.class
})
class MessageAcknowledgementTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    private StubbedNemsEventHandler stubbedNemsEventHandler;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

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
