package gov.samhsa.c2s.pcm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class AbstractProviderDto {
    @Valid
    @NotEmpty
    protected Set<IdentifierDto> identifiers = new HashSet<>();
    @Valid
    protected AddressDto address;
    protected ProviderType providerType;

    enum ProviderType {
        PRACTITIONER, ORGANIZATION
    }
}
