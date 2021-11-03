package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.springframework.stereotype.Service;

@Service
public class NemsEventParser {
    public NemsEventMessage parse(String messageBody) {
        if (messageBody.contains("generalPractitioner")) {
            return NemsEventMessage.nonDeduction();
        }

        String nhsNumber = messageXml.xpath("//fhir:Patient/fhir:identifier/fhir:value/@value").get(0);

        return NemsEventMessage.deduction(nhsNumber);
    }
}
