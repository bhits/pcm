package gov.samhsa.c2s.pcm.service.fhir;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;


public interface FhirConsentService {
    byte[] publishAndGetAttestedFhirConsent(Consent consent, PatientDto patient);

    byte[] revokeAndGetRevokedFhirConsent(Consent consent, PatientDto patient);
}
