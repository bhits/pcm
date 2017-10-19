package gov.samhsa.c2s.pcm.infrastructure.dto;

import gov.samhsa.c2s.pcm.domain.Consent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientConsentDto {

    private PatientDto patient;

    private Consent consent;

}
