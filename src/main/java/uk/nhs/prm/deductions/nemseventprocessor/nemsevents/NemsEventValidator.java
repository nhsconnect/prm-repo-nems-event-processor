package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NemsEventValidator {
    public void validate(String nhsNumber, String nhsNumberValidationValue, String odsCode) {
        validateNhsNumber(nhsNumber, nhsNumberValidationValue);
        validatePreviousGpOdsCode(odsCode);
    }

    private void validateNhsNumber(String nhsNumber, String validationValue) {
        if (nhsNumber.length() != 10) {
            throw new NemsEventValidationException("NHS Number is not 10 digits");
        } else if (!validationValue.equalsIgnoreCase("01")) {
            throw new NemsEventValidationException("NHS Number verification code does not equal 01");
        }
    }

    private void validatePreviousGpOdsCode(String odsCode) {
        if (odsCode.length() > 10) {
            throw new NemsEventValidationException("Previous GP ODS Code is more than 10 characters");
        }
    }
}
