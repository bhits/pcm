package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nMessage;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface I18nService {
    @Transactional
    Optional<I18nMessage> getPurposeOfUseI18nDisplay(String id);

    @Transactional
    Optional<I18nMessage> getPurposeOfUseI18nDescription(String id);

    @Transactional
    Optional<I18nMessage> getConsentRevocationTermI18nText(String id);

    @Transactional
    Optional<I18nMessage> getConsentAttestationTermI18nText(String id);

}
