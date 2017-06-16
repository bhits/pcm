package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.common.consentgen.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
public class ConsentExportServiceImpl implements ConsentExportService {

    private final ConsentService consentService;
    private final UmsService umsService;
    private final ConsentBuilder consentBuilder;

    public ConsentExportServiceImpl(ConsentService consentService, UmsService umsService, ConsentBuilder consentBuilder) {
        this.consentService = consentService;
        this.umsService = umsService;
        this.consentBuilder = consentBuilder;
    }


    @Override
    public Object exportConsent2XACML(XacmlRequestDto xacmlRequestDto) throws ConsentGenException {

        final gov.samhsa.c2s.pcm.service.dto.ConsentDto pcmConsentDto = (gov.samhsa.c2s.pcm.service.dto.ConsentDto)
                consentService.searchConsent(xacmlRequestDto);
        final gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto pcmPatientDto = umsService.getPatientProfile
                (xacmlRequestDto.getPatientId().getExtension());


        final ConsentDto consentGenConsentDto = convertPcmConsentDtoToConsentGenConsentDto(pcmConsentDto,
                pcmPatientDto);


        return new ConsentXacmlDto(pcmConsentDto.getConsentReferenceId(), consentBuilder.buildConsent2Xacml
                (consentGenConsentDto) );
    }

    private ConsentDto convertPcmConsentDtoToConsentGenConsentDto(gov.samhsa.c2s.pcm.service.dto.ConsentDto
                                                                          pcmConsentDto, gov.samhsa.c2s.pcm
                                                                          .infrastructure.dto.PatientDto
            pcmPatientDto) {

        ConsentDto consentDto = new ConsentDto();
        PatientDto patientDto = new PatientDto();

        patientDto.setMedicalRecordNumber(pcmPatientDto.getMrn());
        patientDto.setLastName(pcmPatientDto.getLastName());
        patientDto.setFirstName(pcmPatientDto.getFirstName());

            /* Map PCM consent fields to ConsentGen ConsentDto fields */

        // Map patient Dto
        consentDto.setPatientDto(patientDto);

        // Map consent reference ID
        consentDto.setConsentReferenceid(pcmConsentDto.getConsentReferenceId());

        // Map consent start, end, and signed dates
        consentDto.setConsentStart(Date.from(pcmConsentDto.getStartDate().atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
        consentDto.setConsentEnd(Date.from(pcmConsentDto.getEndDate().atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
        consentDto.setSignedDate(Date.from(pcmConsentDto.getEndDate().atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

/*            // Map providers permitted to disclose (i.e. "from" providers)
            consentDto = mapProvidersPermittedToDisclose(consentDto, fhirConsent);

            // Map providers to which disclosure is made (i.e. "to" providers)
            consentDto = mapProvidersDisclosureIsMadeTo(consentDto, fhirConsent);

            // Map share for purpose of use codes
            consentDto = mapShareForPurposeOfUseCodes(consentDto, fhirConsent);

            // Map share sensitivity policy codes
            consentDto = mapShareSensitivityPolicyCodes(consentDto, fhirConsent);*/

        return consentDto;

    }

}
