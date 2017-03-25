package gov.samhsa.c2s.pcm.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDto {

    @NotNull
    private Long id;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    @NotBlank
    @Pattern(regexp = "^[\\w-]+(\\.[\\w-]+)*@([a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*?\\.[a-zA-Z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$")
    private String email;

    @Past
    private Date birthDate;

    @NotBlank
    private String genderCode;

    private String socialSecurityNumber;
    private String telephone;
    private String address;
    private String city;
    private String stateCode;
    private String zip;

    private List<PatientIdentifierDto> patientIdentifiers;
    private String resourceIdentifier;
    //private String medicalRecordNumber;
    private String enterpriseIdentifier;


}
