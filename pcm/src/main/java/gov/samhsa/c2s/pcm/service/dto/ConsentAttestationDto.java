package gov.samhsa.c2s.pcm.service.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ConsentAttestationDto {
    @Valid
    @NotNull
    private Boolean acceptTerms;

    private ConsentDto consentDto;

}
