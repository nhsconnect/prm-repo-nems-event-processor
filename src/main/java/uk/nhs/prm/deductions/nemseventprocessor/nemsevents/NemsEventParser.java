package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NemsEventParser {
    public NemsEventMessage parse(final String messageBody) {
        try {
            final XML messageXml = parseMessageXML(messageBody);
            if (hasNoPatientEntry(messageXml)) {
                return NemsEventMessage.nonDeduction();
            }

            if (hasNoGpEntry(messageXml)) {
                return createDeductionMessage(messageXml);
            }
            return NemsEventMessage.nonDeduction();
        } catch (RuntimeException exception) {
            log.info("Failed to parse NEMS event message", exception);
            throw new NemsEventParseException(exception);
        }
    }

    @NotNull
    private NemsEventMessage createDeductionMessage(final XML messageXml) {
        final String previousGpReferenceUrl = extractPreviousGpUrl(messageXml);
        final XML organizationXml = findOrganizationByUrl(messageXml, previousGpReferenceUrl);

        return NemsEventMessage.deduction(extractNhsNumber(messageXml),
                extractWhenLastUpdated(messageXml),
                extractOdsCode(organizationXml));
    }

    private String extractPreviousGpUrl(XML messageXml) {
        return query(messageXml, "//fhir:EpisodeOfCare[fhir:status/@value='finished']/fhir:managingOrganization/fhir:reference/@value");
    }

    private XML findOrganizationByUrl(XML messageXml, String organizationUrl) {
        return messageXml.nodes("//fhir:entry[fhir:fullUrl/@value='" + organizationUrl + "']/fhir:resource/fhir:Organization").get(0);
    }

    private String extractOdsCode(XML organizationXml) {
        return query(organizationXml, "fhir:identifier[contains(fhir:system/@value,'ods-organization-code')]/fhir:value/@value");
    }

    private String extractNhsNumber(XML messageXml) {
        return query(messageXml, "//fhir:Patient/fhir:identifier/fhir:value/@value");
    }

    private String extractWhenLastUpdated(XML messageXml) {
        return query(messageXml, "//fhir:MessageHeader/fhir:meta/fhir:lastUpdated/@value");
    }

    private boolean hasNoPatientEntry(XML messageXml) {
        return messageXml.nodes("//fhir:Patient").isEmpty();
    }

    private boolean hasNoGpEntry(XML messageXml) {
        return messageXml.nodes("//fhir:Patient/fhir:generalPractitioner").isEmpty();
    }

    @NotNull
    private XML parseMessageXML(String messageBody) {
        return new XMLDocument(messageBody).registerNs("fhir", "http://hl7.org/fhir");
    }

    private String query(XML messageXml, String query) {
        return messageXml.xpath(query).get(0);
    }
}
