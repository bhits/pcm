package gov.samhsa.c2s.pcm.service.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class PurposeDto {
    private String display;

    @NotNull
    private Long id;

    @NotNull
    private String system;

    @NotNull
    private String value;
}
