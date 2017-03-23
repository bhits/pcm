package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findOneById(Long id);

    Optional<Provider> findOneByIdentifierSystemAndIdentifierValue(String system, String value);

    boolean existsByIdentifierSystemAndIdentifierValue(String system, String value);

    default boolean notExistsByIdentifierSystemAndIdentifierValue(String system, String value) {
        return !existsByIdentifierSystemAndIdentifierValue(system, value);
    }
}
