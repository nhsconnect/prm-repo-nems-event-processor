package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


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

    NemsEventParser nemsEventParser;

    @BeforeEach
    void setUp() {
        nemsEventParser = new NemsEventParser();
    }

    @Test
    void shouldParseANemsMessageAsADeductionWhenGPFieldIsMissing() {
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

        assertTrue(message.isDeduction());
    }

    @Test
    void shouldTreatAMessageThatIsNotAFhirMessageAsANonDeduction() {
        NemsEventMessage message = nemsEventParser.parse("<anyOldMessage></anyOldMessage>");

        assertFalse(message.isDeduction());
    }

    @Test
    void shouldTreatAMessageThatDoesNotHaveAFhirPatientEntryAsANonDeduction() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "    <entry>\n" +
                "        <resource></resource>\n" +
                "    </entry>\n" +
                "</Bundle>";

        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertFalse(message.isDeduction());
    }

    @Test
    void shouldParseANemsMessageAsANonDeductionWhenGPFieldIsPresentInThePatientSection() {
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

        assertFalse(message.isDeduction());
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

        assertThat(message.getNhsNumber()).isEqualTo("9912003888");
    }

    @Test
    void shouldParseANemsMessageAsADeductionWhenGPFieldIsPresentOnlyInNonPatientEntriesInTheMessage() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <meta>\n" +
                "                    <profile value=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1\"/>\n" +
                "                </meta>\n" +
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

        assertTrue(message.isDeduction());
    }

    @Test
    void shouldExtractLastUpdatedFieldWhenParsingADeductionMessageSoThatItCanBeUsedToWorkOutTheLatestNemsEvent() {
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

        assertTrue(message.isDeduction());
        assertThat(message.getLastUpdated()).isEqualTo(new DateTime("2017-11-01T15:00:33+00:00"));
    }

    @Test
    void shouldExtractGPPracticeURLFieldWhenParsingADeductionMessage() {
        String messageBody = "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                LAST_UPDATED +
                "   <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <meta>\n" +
                "                    <profile value=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1\"/>\n" +
                "                </meta>\n" +
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

        assertTrue(message.isDeduction());
        assertThat(message.exposeSensitiveData().get("previousOdsCode")).isEqualTo("B85612");
    }
}
