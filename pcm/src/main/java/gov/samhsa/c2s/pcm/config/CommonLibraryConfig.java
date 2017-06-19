package gov.samhsa.c2s.pcm.config;

import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonLibraryConfig {


    @Bean
    public SimpleMarshaller simpleMarshaller(){
        return new SimpleMarshallerImpl();
    }

    @Bean
    public XmlTransformer xmlTransformer(){
        return new XmlTransformerImpl(simpleMarshaller());
    }
}