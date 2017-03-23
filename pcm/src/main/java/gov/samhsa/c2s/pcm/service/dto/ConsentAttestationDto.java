package gov.samhsa.c2s.pcm.service.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ConsentAttestationDto {
    @Valid
    @NotNull
    private boolean acceptTerms;

    private ConsentDto consentDto;

}
