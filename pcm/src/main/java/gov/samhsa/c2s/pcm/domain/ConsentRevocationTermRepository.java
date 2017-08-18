package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRevocationTermRepository extends JpaRepository<ConsentRevocationTerm, Long> {
    Optional<ConsentRevocationTerm> findById(Long id);
}
