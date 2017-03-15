package gov.samhsa.c2s.pcm.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ProviderIdentifierDto {
    @NotBlank
    private String system;
    @NotBlank
    private String value;
}