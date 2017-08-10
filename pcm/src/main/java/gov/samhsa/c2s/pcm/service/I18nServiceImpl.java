package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nMessage;
import gov.samhsa.c2s.pcm.domain.I18nMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class I18nServiceImpl implements I18nService {
    @Autowired
    I18nMessageRepository i18nMessageRepository;

    @Override
    public Optional<I18nMessage> getI18nMessage(String className, String id, String methodName) {
        String locale = LocaleContextHolder.getLocale().getLanguage();
        String key = className.concat(".").concat(id).concat(".").concat(methodName);
        return i18nMessageRepository.findByKeyAndLocale( key, locale );
    }
}