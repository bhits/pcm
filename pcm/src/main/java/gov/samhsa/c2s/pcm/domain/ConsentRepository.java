package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(Long consentId, Long patientId);
}
