package gov.samhsa.c2s.pcm.infrastructure.pdf;

import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;

import java.util.Date;
import java.util.Optional;

public interface ConsentRevocationPdfGenerator {

    byte[] generateConsentRevocationPdf(Consent consent, PatientDto patient, Date attestedOnDateTime, String consentRevocationTerm, Optional<UserDto> revokedByUserDto);

}
