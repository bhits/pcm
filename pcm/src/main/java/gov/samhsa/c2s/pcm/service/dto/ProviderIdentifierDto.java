package gov.samhsa.c2s.pcm.service.dto;

import lombok.Data;

@Data
public class ProviderIdentifierDto {
    private String system;
    private String value;
}
