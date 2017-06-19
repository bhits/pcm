package gov.samhsa.c2s.pcm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XacmlRequestDto {
    @NotBlank
    private String recipientNpi;

    @NotBlank
    private String intermediaryNpi;

    @NotNull
    private SubjectPurposeOfUseDto purposeOfUse;

    @NotNull
    private PatientIdDto patientId;

}
