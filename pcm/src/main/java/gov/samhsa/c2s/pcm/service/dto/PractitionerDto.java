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
        super(new HashSet<>(), null, ProviderType.PRACTITIONER);
    }

    @Builder
    public PractitionerDto(AddressDto addressDto, Set<IdentifierDto> identifiers, String firstName, String middleName, String lastName) {
        super(identifiers, addressDto, ProviderType.PRACTITIONER);
        this.identifiers = identifiers;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }
}
