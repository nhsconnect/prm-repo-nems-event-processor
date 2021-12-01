package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NemsEventParser {
    public NemsEventMessage parse(final String messageBody) {
        try {
            log.info("Parsing message");
            return tryToParse(messageBody);
        } catch (RuntimeException exception) {
            log.info("Failed to parse NEMS event message", exception);
            throw new NemsEventParseException(exception);
        }
    }

    @NotNull
    private NemsEventMessage tryToParse(String messageBody) {
        final XML messageXml = parseMessageXML(messageBody);
        if (hasNoPatientEntry(messageXml)) {
            log.warn("NEMS event has no patient entry");
            return NemsEventMessage.nonSuspension();
        }

        if (hasNoGpEntry(messageXml)) {
            log.info("NEMS event has no current GP");
            return createSuspensionMessage(messageXml);
        }

        return NemsEventMessage.nonSuspension();
    }

    @NotNull
    private NemsEventMessage createSuspensionMessage(final XML messageXml) {
        final String previousGpReferenceUrl = extractPreviousGpUrl(messageXml);
        final XML organizationXml = findOrganizationByUrl(messageXml, previousGpReferenceUrl);

        return NemsEventMessage.suspension(extractNhsNumber(messageXml),
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
        try {
            return new XMLDocument(messageBody).registerNs("fhir", "http://hl7.org/fhir");
        } catch (IllegalArgumentException exception) {
            log.info("Failed to parse message - invalid XML: {}", exception.getMessage());
            throw new NemsEventParseException(exception);
        }
    }

    private String query(XML messageXml, String query) {
        List<String> xpath = messageXml.xpath(query);
        if (xpath.size() > 1) {
            throw new NemsEventParseException("More than a single instance found of: " + query);
        }
        return xpath.get(0);
    }
}
