package gov.samhsa.c2s.pcm.config;

import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentBuilderImpl;
import gov.samhsa.c2s.common.consentgen.ConsentDtoFactory;
import gov.samhsa.c2s.common.consentgen.XacmlXslUrlProvider;
import gov.samhsa.c2s.common.consentgen.pg.XacmlXslUrlProviderImpl;
import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsentgenConfig {

    @Value("${c2s.pcm.fhir.mrn.oid}")
    private String pcmOrg;

    @Bean
    public ConsentBuilder consentBuilder(ConsentDtoFactory consentDtoFactory,
                                         XmlTransformer xmlTransformer) {
        return new ConsentBuilderImpl(pcmOrg,
                xacmlXslUrlProvider(),
                consentDtoFactory,
                xmlTransformer);
    }

    @Bean
    public XacmlXslUrlProvider xacmlXslUrlProvider() {
        return new XacmlXslUrlProviderImpl();
    }

}