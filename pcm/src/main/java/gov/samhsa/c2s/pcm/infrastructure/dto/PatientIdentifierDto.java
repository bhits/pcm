package gov.samhsa.c2s.pcm.infrastructure.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientIdentifierDto {

    @NotBlank
    private String system;
    @NotBlank
    private String value;

    private String label;
    private String use = "usual";
    private String oid;

}
