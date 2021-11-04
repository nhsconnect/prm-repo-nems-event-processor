package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class NemsEventParser {
    public NemsEventMessage parse(String messageBody) {
        XML messageXml = parseMessageXML(messageBody);
        if (hasNoPatient(messageXml)) {
            return NemsEventMessage.nonDeduction();
        }

        if (hasNoGP(messageXml)) {
            return createDeductionMessage(messageXml);
        }
        return NemsEventMessage.nonDeduction();

    }

    @NotNull
    private NemsEventMessage createDeductionMessage(XML messageXml) {
        String nhsNumber = messageXml.xpath("//fhir:Patient/fhir:identifier/fhir:value/@value").get(0);
        String lastUpdated = messageXml.xpath("//fhir:MessageHeader/fhir:meta/fhir:lastUpdated/@value").get(0);
        String previousGpReferenceUrl = messageXml.xpath("//fhir:EpisodeOfCare[fhir:status/@value='finished']/fhir:managingOrganization/fhir:reference/@value").get(0);
        String odsCode = messageXml.xpath("//fhir:entry[fhir:fullUrl/@value='" + previousGpReferenceUrl + "']/fhir:resource/fhir:Organization/fhir:identifier[contains(fhir:system/@value,'ods-organization-code')]/fhir:value/@value").get(0);

        return NemsEventMessage.deduction(nhsNumber, lastUpdated, odsCode);
    }

    @NotNull
    private XML parseMessageXML(String messageBody) {
        return new XMLDocument(messageBody).registerNs("fhir", "http://hl7.org/fhir");
    }

    private boolean hasNoPatient(XML messageXml) {
        return messageXml.nodes("//fhir:Patient").isEmpty();
    }

    private boolean hasNoGP(XML messageXml) {
        return messageXml.nodes("//fhir:Patient/fhir:generalPractitioner").isEmpty();
    }
}
