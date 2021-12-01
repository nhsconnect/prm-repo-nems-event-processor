package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class NemsEventParserTest {

    private static final String PREVIOUS_GP_ORGANIZATION =
            "    <entry>\n" +
            "        <fullUrl value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
            "        <resource>\n" +
            "            <Organization>\n" +
            "                <identifier>\n" +
            "                    <system value=\"https://fhir.nhs.uk/Id/ods-organization-code\"/>\n" +
            "                    <value value=\"B85612\"/>\n" +
            "                </identifier>\n" +
            "            </Organization>\n" +
            "        </resource>\n" +
            "    </entry>";

    private static final String EPISODE_OF_CARE =
            "   <entry>\n" +
            "        <resource>\n" +
            "            <EpisodeOfCare>\n" +
            "                <status value=\"finished\"/>\n" +
            "                <managingOrganization>\n" +
            "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
            "                </managingOrganization>\n" +
            "            </EpisodeOfCare>\n" +
            "        </resource>\n" +
            "    </entry>\n";
    public static final String LAST_UPDATED = "   <entry>\n" +
            "        <resource>\n" +
            "            <MessageHeader>\n" +
            "                <meta>\n" +
            "                    <lastUpdated value=\"2017-11-01T15:00:33+00:00\"/>\n" +
            "                </meta>\n" +
            "            </MessageHeader>\n" +
            "        </resource>\n" +
            "    </entry>";

    public static final String PATIENT_ENTRY = "    <entry>\n" +
            "        <resource>\n" +
            "            <Patient>\n" +
            "                <identifier>\n" +
            "                    <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n" +
            "                    <value value=\"9912003888\"/>\n" +
            "                </identifier>\n" +
            "            </Patient>\n" +
            "        </resource>\n" +
            "    </entry>\n";

    NemsEventParser nemsEventParser;

    @BeforeEach
    void setUp() {
        nemsEventParser = new NemsEventParser();
    }

    @Test
    void shouldParseANemsMessageAsASuspensionWhenGPFieldIsMissing() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
    }

    @Test
    void shouldTreatAMessageThatIsNotAFhirMessageAsANonSuspension() {
        assertThrows(NemsEventParseException.class, () -> nemsEventParser.parse("should fail as not xml"));
    }

    @Test
    void shouldTreatAMessageThatDoesNotHaveAFhirPatientEntryAsANonSuspension() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "    <entry>\n" +
                "        <resource></resource>\n" +
                "    </entry>\n" +
                "</Bundle>";

        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertFalse(message.isSuspension());
    }

    @Test
    void shouldFailToParseIfThereIsMoreThanOnePatientEntry() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                PATIENT_ENTRY +
                PATIENT_ENTRY +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        assertThrows(NemsEventParseException.class, () -> nemsEventParser.parse(messageBody));
    }

    @Test
    void shouldParseANemsMessageAsANonSuspensionWhenGPFieldIsPresentInThePatientSection() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <generalPractitioner>\n" +
                "                    <reference value=\"urn:uuid:59a63170-b769-44f7-acb1-95cc3a0cb067\"/>\n" +
                "                    <display value=\"SHADWELL MEDICAL CENTRE\"/>\n" +
                "                </generalPractitioner>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "</Bundle>";

        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertFalse(message.isSuspension());
    }

    @Test
    void shouldParseNhsNumberFromANemsMessagePatientEntry() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertThat(message.exposeSensitiveData().get("nhsNumber")).isEqualTo("9912003888");
    }

    @Test
    void shouldParseANemsMessageAsASuspensionWhenGPFieldIsPresentOnlyInNonPatientEntriesInTheMessage() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Organization>\n" +
                "                <generalPractitioner>\n" +
                "                    <reference value=\"urn:uuid:59a63170-b769-44f7-acb1-1234abcd\"/>\n" +
                "                    <display value=\"SHUDEHILL MEDICAL CENTRE\"/>\n" +
                "                </generalPractitioner>\n" +
                "            </Organization>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        NemsEventParser nemsEventParser = new NemsEventParser();
        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
    }

    @Test
    void shouldExtractLastUpdatedFieldWhenParsingASuspensionMessageSoThatItCanBeUsedToWorkOutTheLatestNemsEvent() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <MessageHeader>\n" +
                "                <meta>\n" +
                "                    <lastUpdated value=\"2017-11-01T15:00:33+00:00\"/>\n" +
                "                </meta>\n" +
                "                <timestamp value=\"2019-11-01T15:00:00+00:00\"/>\n" +
                "            </MessageHeader>\n" +
                "        </resource>\n" +
                "    </entry>" +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                EPISODE_OF_CARE +
                PREVIOUS_GP_ORGANIZATION +
                "</Bundle>";

        NemsEventParser nemsEventParser = new NemsEventParser();
        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
        assertThat(message.exposeSensitiveData().get("lastUpdated")).isEqualTo("2017-11-01T15:00:33+00:00");
    }

    @Test
    void shouldExtractGPPracticeURLFieldWhenParsingASuspensionMessage() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n" +
                "                    <value value=\"9912003888\"/>\n" +
                "                </identifier>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <EpisodeOfCare>\n" +
                "                <status value=\"finished\"/>\n" +
                "                <managingOrganization>\n" +
                "                    <reference value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "                </managingOrganization>\n" +
                "            </EpisodeOfCare>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "    <entry>\n" +
                "        <fullUrl value=\"urn:uuid:e84bfc04-2d79-451e-84ef-a50116506088\"/>\n" +
                "        <resource>\n" +
                "            <Organization>\n" +
                "                <identifier>\n" +
                "                    <system value=\"https://fhir.nhs.uk/Id/ods-organization-code\"/>\n" +
                "                    <value value=\"B85612\"/>\n" +
                "                </identifier>\n" +
                "            </Organization>\n" +
                "        </resource>\n" +
                "    </entry>" +
                "</Bundle>";


        NemsEventParser nemsEventParser = new NemsEventParser();
        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertTrue(message.isSuspension());
        assertThat(message.exposeSensitiveData().get("previousOdsCode")).isEqualTo("B85612");
    }

    @Test
    void shouldThrowParsingExceptionWhenPassedNonXml() {
        NemsEventParser nemsEventParser = new NemsEventParser();

        assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse("not-an-xml");
        });
    }

    @Test
    void shouldIncludeCauseOfErrorInParsingExceptionWhenPassedNonXml() {
        NemsEventParser nemsEventParser = new NemsEventParser();

        Throwable nemsEventParseException = assertThrows(NemsEventParseException.class, () -> {
            nemsEventParser.parse("not-an-xml");
        });

        assertThat(nemsEventParseException.getCause().getMessage()).contains("Can't parse");
    }
}
