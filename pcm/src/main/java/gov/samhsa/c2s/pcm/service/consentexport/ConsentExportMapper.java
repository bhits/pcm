package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;

public interface ConsentExportMapper {
    ConsentDto map(DetailedConsentDto
                           pcmDetailedConsentDto, gov.samhsa.c2s.pcm
                           .infrastructure.dto.PatientDto
                           pcmPatientDto);
}
