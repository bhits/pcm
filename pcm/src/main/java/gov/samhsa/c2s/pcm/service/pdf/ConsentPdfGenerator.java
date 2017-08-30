package gov.samhsa.c2s.pcm.service.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public interface ConsentPdfGenerator {

    byte[] generateConsentPdf(Consent consent, PatientDto patientDto, Date operatedOnDateTime, String consentTerms, Optional<UserDto> operatedByUserDto, Optional<Boolean> operatedByPatient) throws IOException;
}
