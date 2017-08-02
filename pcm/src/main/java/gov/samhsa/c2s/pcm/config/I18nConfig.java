package gov.samhsa.c2s.pcm.config;

import gov.samhsa.c2s.pcm.infrastructure.i18n.HorizontalDatabaseMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class I18nConfig {

    @Bean
    public HorizontalDatabaseMessageSource horizontalDatabaseMessageSource(){
        return new HorizontalDatabaseMessageSource();
    }
}