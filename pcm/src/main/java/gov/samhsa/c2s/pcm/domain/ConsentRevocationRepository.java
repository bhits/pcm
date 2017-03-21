package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentRevocationRepository extends JpaRepository<ConsentRevocation, Long> {
}
