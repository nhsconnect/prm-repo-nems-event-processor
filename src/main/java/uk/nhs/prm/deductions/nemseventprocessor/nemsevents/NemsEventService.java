package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.nemseventprocessor.audit.AuditService;
import uk.nhs.prm.deductions.nemseventprocessor.dlq.DeadLetterQueuePublisher;
import uk.nhs.prm.deductions.nemseventprocessor.suspensions.SuspensionsEventPublisher;
import uk.nhs.prm.deductions.nemseventprocessor.unhandledevents.UnhandledEventPublisher;

import static uk.nhs.prm.deductions.nemseventprocessor.audit.AuditMessageStatus.NO_ACTION_NON_SUSPENSION;

@Service
@Slf4j
@RequiredArgsConstructor
public class NemsEventService {

    private final NemsEventParser parser;
    private final UnhandledEventPublisher unhandledEventPublisher;
    private final SuspensionsEventPublisher suspensionsEventPublisher;
    private final DeadLetterQueuePublisher deadLetterQueuePublisher;
    private final AuditService auditService;

    public void processNemsEvent(String message) {
        try {
            auditService.extractNemsMessageIdAndPublishAuditMessage(message);
            NemsEventMessage nemsEventMessage = parser.parse(message);
            if (nemsEventMessage.isSuspension()) {
                log.info("SUSPENSION event - sending to suspensions sns topic");
                suspensionsEventPublisher.sendMessage(nemsEventMessage);
                return;
            }
            log.info("NON-SUSPENSION event - sending to unhandled sns topic");
            NonSuspendedMessage nonSuspendedMessage = new NonSuspendedMessage(nemsEventMessage.getNemsMessageId(), NO_ACTION_NON_SUSPENSION);
            unhandledEventPublisher.sendMessage(nonSuspendedMessage, "Non-suspension");
        }
        catch (Exception e) {
                log.info("PROCESSING FAILED - sending to dead letter sns topic.", e);
            deadLetterQueuePublisher.sendMessage(message, e.getMessage());
        }
    }
}
