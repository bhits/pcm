package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentDtoFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ConsentDtoFactoryImpl implements ConsentDtoFactory {

    public ConsentDtoFactoryImpl(){
        super();
    }

    @Override
    public ConsentDto createConsentDto(long consentId) {
        return null;
    }

    @Override
    public ConsentDto createConsentDto(Object obj) {
        return (ConsentDto) obj;
    }
}
