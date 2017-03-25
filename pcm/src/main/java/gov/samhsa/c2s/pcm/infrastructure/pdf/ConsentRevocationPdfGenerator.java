package gov.samhsa.c2s.pcm.infrastructure.pdf;

import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;

import java.util.Date;

public interface ConsentRevocationPdfGenerator {

    byte[] generateConsentRevocationPdf(Consent consent, PatientDto patient, Date attestedOnDateTime, String consentRevocationTerm);

}
