package gov.samhsa.c2s.pcm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class IdentifiersDto {
    @Valid
    @NotEmpty
    private Set<IdentifierDto> identifiers = new HashSet<>();

    public static IdentifiersDto of(IdentifierDto... identifierDtos) {
        return of(Arrays.stream(identifierDtos).collect(toSet()));
    }
}