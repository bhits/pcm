package gov.samhsa.c2s.pcm.service.fhir;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;


public interface FhirConsentService {
    public byte[] getAttestedFhirConsent(Consent consent, PatientDto patient, boolean isPublishEnabled);
    public byte[] getRevokedFhirConsent(Consent consent, PatientDto patient, boolean isPublishEnabled);
}
