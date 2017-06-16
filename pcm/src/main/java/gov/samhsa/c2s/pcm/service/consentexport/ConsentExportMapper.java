package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.pcm.domain.Consent;

public interface ConsentExportMapper {
    ConsentDto map(gov.samhsa.c2s.pcm.service.dto.ConsentDto
                           pcmConsentDto, gov.samhsa.c2s.pcm
                           .infrastructure.dto.PatientDto
                           pcmPatientDto) ;

}
