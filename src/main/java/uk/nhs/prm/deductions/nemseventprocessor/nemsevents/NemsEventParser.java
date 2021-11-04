package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.springframework.stereotype.Service;

@Service
public class NemsEventParser {
    public NemsEventMessage parse(String messageBody) {
        XML messageXml = new XMLDocument(messageBody).registerNs("fhir", "http://hl7.org/fhir");

        if (! messageXml.nodes("//fhir:Patient/fhir:generalPractitioner").isEmpty()) {
            return NemsEventMessage.nonDeduction();
        }

        if (messageXml.nodes("//fhir:Patient").isEmpty()) {
            return NemsEventMessage.nonDeduction();
        }

        String nhsNumber = messageXml.xpath("//fhir:Patient/fhir:identifier/fhir:value/@value").get(0);
        String lastUpdated = messageXml.xpath("//fhir:MessageHeader/fhir:meta/fhir:lastUpdated/@value").get(0);

        return NemsEventMessage.deduction(nhsNumber, lastUpdated);
    }
}
