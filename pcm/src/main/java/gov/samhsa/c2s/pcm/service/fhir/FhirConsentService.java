package gov.samhsa.c2s.pcm.service.fhir;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;


public interface FhirConsentService {
    public byte[] publishFhirConsent(Consent consent,PatientDto patient, boolean isEnabled);
    public byte[] revokeFhirConsent(Consent consent,PatientDto patient, boolean isEnabled);
}
