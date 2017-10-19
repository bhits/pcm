package gov.samhsa.c2s.pcm.service.dto;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class PatientDtoDetailedConsentDtoPair {
    private PatientDto patientDto;
    private DetailedConsentDto detailedConsentDto;
}
