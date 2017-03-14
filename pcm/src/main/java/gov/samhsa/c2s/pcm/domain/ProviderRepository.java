package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider, Identifier> {
}
