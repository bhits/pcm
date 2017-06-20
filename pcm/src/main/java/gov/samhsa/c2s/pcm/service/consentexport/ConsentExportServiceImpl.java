package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.pcm.config.FhirProperties;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.pcm.service.exception.ConsentExportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsentExportServiceImpl implements ConsentExportService {

    private final ConsentService consentService;
    private final UmsService umsService;
    private final ConsentBuilder consentBuilder;
    private final FhirProperties fhirProperties;

    public ConsentExportServiceImpl(ConsentService consentService, UmsService umsService, ConsentBuilder
            consentBuilder, FhirProperties
                                            fhirProperties) {
        this.consentService = consentService;
        this.umsService = umsService;
        this.consentBuilder = consentBuilder;
        this.fhirProperties = fhirProperties;
    }


    @Override
    public ConsentXacmlDto exportConsent2XACML(XacmlRequestDto xacmlRequestDto) {
        log.debug("Invoking consent export Service - exportConsent2XACML - Start");
        final DetailedConsentDto pcmConsentDto = (DetailedConsentDto)
                consentService.searchConsent(xacmlRequestDto);
        ConsentXacmlDto consentXacmlDto = new ConsentXacmlDto();
        try {
            consentXacmlDto.setConsentRefId(pcmConsentDto.getConsentReferenceId());
            log.debug("Invoking common libraries consentgen - buildConsent2Xacml - Start");
            consentXacmlDto.setConsentXacml(consentBuilder.buildConsent2Xacml(xacmlRequestDto));
            log.debug("Invoking common libraries consentgen - buildConsent2Xacml - End");
        } catch (ConsentGenException e) {
            throw new ConsentExportException(e.getMessage(), e);
        }
        log.debug("Invoking consent export service - exportConsent2XACML - End");
        return consentXacmlDto;

    }

}
