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
    default Optional<Patient> findOneAsOptional(Long id) {
        return Optional.ofNullable(findOne(id));
    }

    default Patient saveAndGet(Long id) {
        return findOneAsOptional(id)
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
