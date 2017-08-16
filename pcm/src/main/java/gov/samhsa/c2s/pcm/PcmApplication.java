package gov.samhsa.c2s.pcm;

import gov.samhsa.c2s.common.i18n.I18nEnabled;
import gov.samhsa.c2s.common.i18n.config.EnableI18nJpaSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan(basePackageClasses = {PcmApplication.class, Jsr310JpaConverters.class})
@EnableAspectJAutoProxy
@EnableJpaAuditing
@EnableI18nJpaSupport
@EnableJpaRepositories(basePackageClasses = {PcmApplication.class, I18nEnabled.class})
public class PcmApplication {

    public static void main(String[] args) {
        SpringApplication.run(PcmApplication.class, args);
    }
}