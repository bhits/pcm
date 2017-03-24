package gov.samhsa.c2s.pcm.service.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Data
public class PractitionerDto extends AbstractProviderDto {

    @NotBlank
    private String firstName;
    private String middleName;
    @NotBlank
    private String lastName;

    public PractitionerDto() {
        super(null, new HashSet<>(), null, null, null, ProviderType.PRACTITIONER);
    }

    @Builder
    public PractitionerDto(AddressDto addressDto, Set<IdentifierDto> identifiers, String firstName, String middleName, String lastName) {
        super(null, identifiers, addressDto, null, null, ProviderType.PRACTITIONER);
        this.identifiers = identifiers;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }
}
