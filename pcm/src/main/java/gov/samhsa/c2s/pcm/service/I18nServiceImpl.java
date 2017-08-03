package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.infrastructure.i18n.HorizontalDatabaseMessageSource;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

@Service
public class I18nServiceImpl implements I18nService {
    private HorizontalDatabaseMessageSource horizontalDatabaseMessageSource;
    private final String PURPOSE = "PURPOSE";
    private final String DISPLAY = "DISPLAY";
    private final String DESCRIPTION = "DESCRIPTION";

    @Autowired
    public I18nServiceImpl(HorizontalDatabaseMessageSource horizontalDatabaseMessageSource) {
        this.horizontalDatabaseMessageSource = horizontalDatabaseMessageSource;
    }

    @Override
    public String getPurposeOfUseI18nDisplay(String value) {
        Locale locale = LocaleContextHolder.getLocale();
        String displayCode = PURPOSE.concat(".").concat(value).concat(".").concat(DISPLAY);
        return horizontalDatabaseMessageSource.getMessage( displayCode,null, locale );
    }

    @Override
    public String getPurposeOfUseI18nDescription(String value) {
        Locale locale = LocaleContextHolder.getLocale();
        String descriptionCode = PURPOSE.concat(".").concat(value).concat(".").concat(DESCRIPTION);
        return horizontalDatabaseMessageSource.getMessage( descriptionCode,null, locale );
    }
}