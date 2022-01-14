package uk.nhs.prm.deductions.nemseventprocessor.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.nemseventprocessor.MessagePublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventPublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    @Test
    void shouldPublishNEMSAuditTopic() {
        String auditTopicArn = "auditTopicArn";
        AuditEventPublisher publisher = new AuditEventPublisher(messagePublisher, auditTopicArn);

        AuditMessage auditMessage = new AuditMessage("someId", "someBody");

        publisher.sendMessage(auditMessage);

        String auditMessageAsString = "{" +
            "\"nemsMessageId\":\"someId\"," +
            "\"messageBody\":\"someBody\"" +
            "}";

        verify(messagePublisher).sendMessage(auditTopicArn, auditMessageAsString);
    }

}
