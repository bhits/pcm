package gov.samhsa.c2s.pcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface I18nMessageRepository extends JpaRepository<I18nMessage, Long> {

}
