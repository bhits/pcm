package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.config.SpringContext;
import gov.samhsa.c2s.pcm.infrastructure.PhrService;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findOneById(Long id);

    Optional<Patient> findOneByIdAndProvidersId(Long patientId, Long providerId);

    Optional<Patient> findOneByIdAndConsentsId(Long patientId, Long consentId);

    Optional<Patient> findOneByIdAndConsentsIdAndConsentsConsentAttestationIsNullAndConsentsConsentRevocationIsNull(Long patientId, Long consentId);

    boolean existsByIdAndConsentsId(Long patientId, Long consentId);

    default boolean notExistsByIdAndConsentsId(Long patientId, Long consentId) {
        return !existsByIdAndConsentsId(patientId, consentId);
    }

    boolean existsByIdAndProvidersId(Long patientId, Long providerId);

    default boolean notExistsByIdAndProvidersId(Long patientId, Long providerId) {
        return !existsByIdAndProvidersId(patientId, providerId);
    }

    boolean existsByIdAndProvidersIdentifierSystemAndProvidersIdentifierValue(Long patientId, String identifierSystem, String identifierValue);

    default boolean notExistsByIdAndProvidersIdentifierSystemAndProvidersIdentifierValue(Long patientId, String identifierSystem, String identifierValue) {
        return !existsByIdAndProvidersIdentifierSystemAndProvidersIdentifierValue(patientId, identifierSystem, identifierValue);
    }

    default Patient saveAndGet(Long id) {
        return findOneById(id)
                .orElseGet(() -> {
                    final PhrService phrService = SpringContext.getBean(PhrService.class);
                    final PatientDto patientProfile = phrService.getPatientProfile();
                    Assert.isTrue(id == patientProfile.getId(), "Invalid patient ID");
                    final Patient p = new Patient();
                    p.setId(id);
                    save(p);
                    return p;
                });
    }
}
