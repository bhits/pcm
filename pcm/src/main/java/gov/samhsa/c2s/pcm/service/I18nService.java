package gov.samhsa.c2s.pcm.service;

import org.springframework.transaction.annotation.Transactional;


public interface I18nService {
    @Transactional
    String getPurposeOfUseI18nDisplay(String value);

    @Transactional
    String getPurposeOfUseI18nDescription(String value);

}
