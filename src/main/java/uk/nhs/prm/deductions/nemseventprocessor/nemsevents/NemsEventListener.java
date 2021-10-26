package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@RequiredArgsConstructor
public class NemsEventListener implements MessageListener {

    private final NemsEventService nemsEventService;


    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        log.info("RECEIVED: Nems Event Message");
        try {
            String payload = ((TextMessage) message).getText();
            nemsEventService.processNemsEvent(payload);
            message.acknowledge();
            log.info("ACKNOWLEDGED: Nems Event Message");
        } catch (JMSException e) {
            log.error("Error while processing message: {}", e.getMessage());
        }
    }
}
