package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nMessage;
import gov.samhsa.c2s.pcm.domain.I18nMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class I18nServiceImpl implements I18nService {
    private final String PURPOSE = "PURPOSE";
    private final String CONSENT_ATTESTATON_TERM = "CONSENT_ATTESTATON_TERM";
    private final String CONSENT_REVOCATION_TERM = "CONSENT_REVOCATION_TERM";
    private final String CONSENT_TERM_TEXT = "TEXT";

    @Autowired
    I18nMessageRepository i18nMessageRepository;

    @Override
    public Optional<I18nMessage> getPurposeOfUseI18nDisplay(String id) {
        String PROPERTY_NAME = "DISPLAY";
        String locale = LocaleContextHolder.getLocale().getLanguage();
        String key = PURPOSE.concat(".").concat(id).concat(".").concat(PROPERTY_NAME);
        return i18nMessageRepository.findByKeyAndLocale( key, locale );
    }

    @Override
    public  Optional<I18nMessage> getPurposeOfUseI18nDescription(String id) {
        String PROPERTY_NAME = "DESCRIPTION";
        String locale = LocaleContextHolder.getLocale().getLanguage();
        String key = PURPOSE.concat(".").concat(id).concat(".").concat(PROPERTY_NAME);
        return i18nMessageRepository.findByKeyAndLocale( key, locale );
    }

    @Override
    public Optional<I18nMessage> getConsentRevocationTermI18nText(String id) {
        String locale = LocaleContextHolder.getLocale().getLanguage();
        String key = CONSENT_REVOCATION_TERM.concat(".").concat(id).concat(".").concat(CONSENT_TERM_TEXT);
        return i18nMessageRepository.findByKeyAndLocale( key, locale );
    }

    @Override
    public Optional<I18nMessage> getConsentAttestationTermI18nText(String id) {
        String locale = LocaleContextHolder.getLocale().getLanguage();
        String key = CONSENT_ATTESTATON_TERM.concat(".").concat(id).concat(".").concat(CONSENT_TERM_TEXT);
        return i18nMessageRepository.findByKeyAndLocale( key, locale );
    }


}