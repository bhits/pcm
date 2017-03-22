package gov.samhsa.c2s.pcm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurposeDto {
    private String description;
    private String display;
    @NotNull
    private Long id;
    @NotNull
    private String system;
    @NotNull
    private String value;
}
