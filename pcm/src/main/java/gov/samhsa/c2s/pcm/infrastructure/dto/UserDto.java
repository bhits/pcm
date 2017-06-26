package gov.samhsa.c2s.pcm.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;

    @NotEmpty
    private String lastName;

    private String middleName;

    @NotEmpty
    private String firstName;

    @Past
    @NotNull
    private LocalDate birthDate;

    @NotEmpty
    private String genderCode;

    private Optional<String> socialSecurityNumber;

    private List<AddressDto> addresses;

    private List<TelecomDto> telecoms;

    private List<RoleDto> roles;

    private String locale;

    private boolean disabled;

    private String mrn;

    private Optional<String> registrationPurposeEmail;

    @Valid
    private Optional<List<IdentifierDto>> identifiers;
}
