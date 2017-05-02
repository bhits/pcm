package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.config.SpringContext;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    Optional<Patient> findOneById(String id);

    Optional<Patient> findOneByIdAndProvidersId(String patientId, Long providerId);

    Optional<Patient> findOneByIdAndConsentsId(String patientId, Long consentId);

    Optional<Patient> findOneByIdAndConsentsIdAndConsentsConsentAttestationIsNullAndConsentsConsentRevocationIsNull(String patientId, Long consentId);

    boolean existsByIdAndConsentsId(String patientId, Long consentId);

    default boolean notExistsByIdAndConsentsId(String patientId, Long consentId) {
        return !existsByIdAndConsentsId(patientId, consentId);
    }

    boolean existsByIdAndProvidersId(String patientId, Long providerId);

    default boolean notExistsByIdAndProvidersId(String patientId, Long providerId) {
        return !existsByIdAndProvidersId(patientId, providerId);
    }

    boolean existsByIdAndProvidersIdentifierSystemAndProvidersIdentifierValue(String patientId, String identifierSystem, String identifierValue);

    default boolean notExistsByIdAndProvidersIdentifierSystemAndProvidersIdentifierValue(String patientId, String identifierSystem, String identifierValue) {
        return !existsByIdAndProvidersIdentifierSystemAndProvidersIdentifierValue(patientId, identifierSystem, identifierValue);
    }

    default Patient saveAndGet(String mrn) {
        return findOneById(mrn)
                .orElseGet(() -> {
                    final UmsService umsService = SpringContext.getBean(UmsService.class);
                    final PatientDto patientProfile = umsService.getPatientProfile(mrn);
                    Assert.isTrue(mrn.equals(patientProfile.getMrn()), "Invalid patient ID");
                    final Patient p = new Patient();
                    p.setId(mrn);
                    save(p);
                    return p;
                });
    }
}
