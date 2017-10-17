package gov.samhsa.c2s.pcm.service.consentexport;

import feign.FeignException;
import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.PatientDtoDetailedConsentDtoPair;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.pcm.service.exception.ConsentExportException;
import gov.samhsa.c2s.pcm.service.exception.PatientNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class ConsentExportServiceImpl implements ConsentExportService {

    public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private final ConsentService consentService;
    private final UmsService umsService;
    private final ConsentBuilder consentBuilder;

    @Autowired
    public ConsentExportServiceImpl(ConsentService consentService, UmsService umsService, ConsentBuilder
            consentBuilder) {
        this.consentService = consentService;
        this.umsService = umsService;
        this.consentBuilder = consentBuilder;
    }

    @Override
    public ConsentXacmlDto exportConsent2XACML(XacmlRequestDto xacmlRequestDto) {
        try {
            log.debug("Invoking consent export Service - exportConsent2XACML - Start");
            final PatientDto patientDto = umsService.getPatientByIdentifierValueAndIdentifierSystem
                    (xacmlRequestDto.getPatientId().getExtension(), xacmlRequestDto.getPatientId().getRoot());
            final DetailedConsentDto detailedConsentDto = consentService.searchConsent(xacmlRequestDto);
            final PatientDtoDetailedConsentDtoPair patientDtoDetailedConsentDtoPair =
                    PatientDtoDetailedConsentDtoPair.of(patientDto, detailedConsentDto);

            log.debug("Invoking common libraries consentgen - buildConsent2Xacml - Start");
            final String consentXacml = consentBuilder.buildConsent2Xacml(patientDtoDetailedConsentDtoPair);
            final byte[] consentXacmlBytes = consentXacml.getBytes(DEFAULT_ENCODING);
            log.debug("Invoking common libraries consentgen - buildConsent2Xacml - End");
            return ConsentXacmlDto.of(detailedConsentDto.getConsentReferenceId(), consentXacmlBytes, DEFAULT_ENCODING.name());
        } catch (ConsentGenException e) {
            throw new ConsentExportException(e.getMessage(), e);
        } catch (FeignException e) {
            throw new PatientNotFoundException(e.getMessage(), e);
        } finally {
            log.debug("Invoking consent export service - exportConsent2XACML - End");
        }
    }
}
