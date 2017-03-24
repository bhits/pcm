package gov.samhsa.c2s.pcm.service.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Data
public class OrganizationDto extends AbstractProviderDto {

    @NotBlank
    private String name;

    public OrganizationDto() {
        super(null, new HashSet<>(), null, null, null, ProviderType.ORGANIZATION);
    }

    @Builder
    public OrganizationDto(AddressDto addressDto, Set<IdentifierDto> identifiers, String name) {
        super(null, identifiers, addressDto, null, null, ProviderType.ORGANIZATION);
        this.identifiers = identifiers;
        this.name = name;
    }
}
