package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentDtoFactory;
import gov.samhsa.c2s.pcm.service.dto.PatientDtoDetailedConsentDtoPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsentDtoFactoryImpl implements ConsentDtoFactory {

    private final ConsentExportMapper consentExportMapper;

    @Autowired
    public ConsentDtoFactoryImpl(ConsentExportMapper consentExportMapper) {
        this.consentExportMapper = consentExportMapper;
    }

    @Override
    public ConsentDto createConsentDto(long consentId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsentDto createConsentDto(Object obj) {
        final PatientDtoDetailedConsentDtoPair patientDtoDetailedConsentDtoPair = (PatientDtoDetailedConsentDtoPair) obj;
        final ConsentDto consentGenConsentDto = consentExportMapper
                .map(patientDtoDetailedConsentDtoPair.getDetailedConsentDto(), patientDtoDetailedConsentDtoPair.getPatientDto());
        return consentGenConsentDto;
    }
}
