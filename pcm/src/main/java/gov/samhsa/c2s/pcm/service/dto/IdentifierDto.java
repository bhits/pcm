package gov.samhsa.c2s.pcm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class IdentifierDto {
    @NotBlank
    private String system;
    @NotBlank
    private String value;
}
