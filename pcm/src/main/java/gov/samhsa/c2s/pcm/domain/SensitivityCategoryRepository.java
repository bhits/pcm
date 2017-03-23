package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensitivityCategoryRepository extends JpaRepository<SensitivityCategory, Long> {
    Optional<SensitivityCategory> findOneByIdentifierSystemAndIdentifierValue(String system, String value);
}
