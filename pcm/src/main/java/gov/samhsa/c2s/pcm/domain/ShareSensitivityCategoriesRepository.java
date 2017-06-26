package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShareSensitivityCategoriesRepository extends JpaRepository<ShareSensitivityCategories, Long> {
    Optional<ShareSensitivityCategories> findOneById(long id);
}
