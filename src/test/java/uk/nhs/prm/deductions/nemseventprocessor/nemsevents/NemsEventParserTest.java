package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import net.logstash.logback.appender.destination.DestinationParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NemsEventParserTest {
    @Test
    void shouldParseANemsMessageAsADeductionWhenGPFieldIsMissing() {
        String messageBody= "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <meta>\n" +
                "                    <profile value=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1\"/>\n" +
                "                </meta>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "</Bundle>";

        NemsEventParser nemsEventParser = new NemsEventParser();
        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertTrue(message.isDeduction());
    }

    @Test
    void shouldParseANemsMessageAsANonDeductionWhenGPFieldIsPresent(){
        String messageBody= "<Bundle xmlns=\"http://hl7.org/fhir\">\n" +
                "    <entry>\n" +
                "        <resource>\n" +
                "            <Patient>\n" +
                "                <meta>\n" +
                "                    <profile value=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1\"/>\n" +
                "                </meta>\n" +
                "                <generalPractitioner>\n" +
                "                    <reference value=\"urn:uuid:59a63170-b769-44f7-acb1-95cc3a0cb067\"/>\n" +
                "                    <display value=\"SHADWELL MEDICAL CENTRE\"/>\n" +
                "                </generalPractitioner>\n" +
                "            </Patient>\n" +
                "        </resource>\n" +
                "    </entry>\n" +
                "</Bundle>";
        NemsEventParser nemsEventParser = new NemsEventParser();
        NemsEventMessage message = nemsEventParser.parse(messageBody);

        assertFalse(message.isDeduction());
    }
}