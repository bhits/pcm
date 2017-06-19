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
    protected Long id;
    @Valid
    @NotEmpty
    protected Set<IdentifierDto> identifiers = new HashSet<>();
    @Valid
    protected AddressDto address;
    protected Boolean deletable;
    protected String phoneNumber;
    /**
     * Immutable property to represent the {@link ProviderType} of this instance
     */
    private ProviderType providerType;

    private void setProviderType(ProviderType providerType) {
        // Make providerType immutable
    }

    public enum ProviderType {
        PRACTITIONER, ORGANIZATION
    }
}
