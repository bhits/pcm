package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nEnabled;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;


public interface I18nService {

    @Transactional
    String getI18nMessage(I18nEnabled entityReference, String fieldName, Supplier<String> defaultMessageSupplier);
}
