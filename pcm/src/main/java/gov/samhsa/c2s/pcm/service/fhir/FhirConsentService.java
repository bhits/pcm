package gov.samhsa.c2s.pcm.service.fhir;

import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;


/**
 * Created by sadhana.chandra on 12/2/2016.
 */
public interface FhirConsentService {
    public void publishFhirConsent(Consent c2sConsent, PatientDto patientDto);
}
