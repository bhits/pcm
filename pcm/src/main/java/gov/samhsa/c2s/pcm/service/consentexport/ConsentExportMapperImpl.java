package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.IndividualProviderDto;
import gov.samhsa.c2s.common.consentgen.PatientDto;
import gov.samhsa.c2s.pcm.domain.Consent;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

@Service
public class ConsentExportMapperImpl implements ConsentExportMapper {


    public ConsentDto map(gov.samhsa.c2s.pcm.service.dto.ConsentDto
                                  pcmConsentDto, gov.samhsa.c2s.pcm
                                  .infrastructure.dto.PatientDto
                                  pcmPatientDto){
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
