package gov.samhsa.c2s.pcm.infrastructure.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;

import java.util.Date;

public interface ConsentPdfGenerator {

    byte[] generate42CfrPart2Pdf(Consent consent, PatientDto patientDto, boolean isSigned, Date attestedOn, String consentTerms);
}
