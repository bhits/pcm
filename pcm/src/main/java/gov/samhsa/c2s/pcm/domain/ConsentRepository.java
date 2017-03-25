package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(Long consentId, Long patientId);

    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNotNullAndConsentRevocationIsNull(Long consentId, Long patientId);

    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNotNullAndConsentRevocationIsNotNull(Long consentId, Long patientId);

    Optional<Consent> findOneByIdAndPatientId(Long consentId, Long patientId);

    Page<Consent> findAllByPatientId(Long patientId, Pageable pageable);


}
