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
        super(new HashSet<>(), null, ProviderType.ORGANIZATION);
    }

    @Builder
    public OrganizationDto(AddressDto addressDto, Set<IdentifierDto> identifiers, String name) {
        super(identifiers, addressDto, ProviderType.ORGANIZATION);
        this.identifiers = identifiers;
        this.name = name;
    }
}
