package gov.samhsa.c2s.pcm.infrastructure.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;

import java.util.Date;
import java.util.Optional;

public interface ConsentPdfGenerator {

    byte[] generate42CfrPart2Pdf(Consent consent, PatientDto patientDto, boolean isSigned, Date attestedOn, String consentTerms, Optional<UserDto> attesterUserDto);
}
