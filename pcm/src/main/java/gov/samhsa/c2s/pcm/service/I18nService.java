package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nMessage;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface I18nService {

    @Transactional
    Optional<I18nMessage> getI18nMessage(String className, String id, String methodName) ;

}
