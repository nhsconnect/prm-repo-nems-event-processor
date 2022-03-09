package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.amazonaws.services.sqs.AmazonSQSAsync;
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
@ContextConfiguration(classes = { LocalStackAwsConfig.class, StubbedNemsEventServiceConfig.class })
class MessageAcknowledgementTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    @Test
    void shouldNotAcknowledgeMessageWhenExceptionIsThrownInProcessingLeavingItAvailableToBeReprocessed() {
        String queueUrl = amazonSQSAsync.getQueueUrl(nemsEventQueueName).getQueueUrl();

        StubbedNemsEventServiceConfig.throwOnProcessNextMessage();

        amazonSQSAsync.sendMessage(queueUrl, "bob");

        StubbedNemsEventServiceConfig.waitUntilProcessedMessage();

        var messagesAfterProcessing = getIncomingNemsMessages();

        assertThat(messagesAfterProcessing.size()).isEqualTo(1);
        assertThat(messagesAfterProcessing.get(0).getBody()).isEqualTo("bob");
    }

    private List<Message> getIncomingNemsMessages() {
        String incomingQueue = amazonSQSAsync.getQueueUrl(nemsEventQueueName).getQueueUrl();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl(incomingQueue);
        return amazonSQSAsync.receiveMessage(receiveMessageRequest).getMessages();
    }

    private void purgeQueue(String queueUrl) {
        System.out.println("Purging queue url: " + queueUrl);
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(queueUrl));
    }
}
