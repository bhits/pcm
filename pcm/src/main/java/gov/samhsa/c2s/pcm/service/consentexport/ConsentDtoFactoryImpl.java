package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentDtoFactory;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ConsentDtoFactoryImpl implements ConsentDtoFactory {

    private final ConsentService consentService;
    private final UmsService umsService;
    private final ConsentExportMapper consentExportMapper;

    public ConsentDtoFactoryImpl(ConsentService consentService, UmsService umsService, ConsentExportMapper
            consentExportMapper) {
        this.consentService = consentService;
        this.umsService = umsService;
        this.consentExportMapper = consentExportMapper;
    }


    @Override
    public ConsentDto createConsentDto(long consentId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsentDto createConsentDto(Object obj) {
        XacmlRequestDto xacmlRequestDto = (XacmlRequestDto)obj;
        final DetailedConsentDto pcmConsentDto = (DetailedConsentDto)
                consentService.searchConsent(xacmlRequestDto);
        final gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto pcmPatientDto = umsService.getPatientProfile
                (xacmlRequestDto.getPatientId().getExtension());


        final ConsentDto consentGenConsentDto = consentExportMapper.map(pcmConsentDto, pcmPatientDto);

        return consentGenConsentDto;
    }
}
