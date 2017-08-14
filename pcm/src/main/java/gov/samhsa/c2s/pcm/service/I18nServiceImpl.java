package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nMessage;
import gov.samhsa.c2s.pcm.domain.I18nMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Optional;

@Service
@Slf4j
public class I18nServiceImpl implements I18nService {

    private final String ID = "id";
    @Autowired
    I18nMessageRepository i18nMessageRepository;

    @Override
    public Optional<I18nMessage> getI18nMessage(Object entity, String fieldName) {
        String key = null;
        try {
            Class<?> clazz = entity.getClass();
            Field field = org.springframework.util.ReflectionUtils.findField(clazz, ID);
            org.springframework.util.ReflectionUtils.makeAccessible(field);
            String id = field.get(entity).toString();

            String locale = LocaleContextHolder.getLocale().getLanguage();

            key = clazz.getSimpleName().toUpperCase().concat(".").concat(id).concat(".").concat(fieldName);
            return i18nMessageRepository.findByKeyAndLocale(key, locale);
        }catch (IllegalAccessException e){
            log.error("Cannot get I18n message for key: " + key);
            return  Optional.empty();
        }
    }
}