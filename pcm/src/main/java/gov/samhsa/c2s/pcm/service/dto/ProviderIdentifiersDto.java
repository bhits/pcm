package gov.samhsa.c2s.pcm.service.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Data
public class ProviderIdentifiersDto {
    @Valid
    private Set<ProviderIdentifierDto> providerIdentifiers = new HashSet<>();
}