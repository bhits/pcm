package gov.samhsa.c2s.pcm.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDto {

    @NotNull
    private String id;

    @NotBlank
    private String lastName;

    private String middleName;

    @NotBlank
    private String firstName;

    @Past
    private LocalDate birthDate;

    @NotBlank
    private String genderCode;

    private String socialSecurityNumber;
    private List<AddressDto> addresses;

    private List<TelecomDto> telecoms;

    private List<PatientIdentifierDto> identifiers;
    private String resourceIdentifier;
    private String mrn;
    private String enterpriseIdentifier;


}
