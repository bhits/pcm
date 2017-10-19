package gov.samhsa.c2s.pcm.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "c2s.pcm")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class PcmProperties {

    @NotNull
    @NotEmpty
    private List<String> supportedProviderSystems;

    @NotNull
    @Valid
    private Consent consent;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Consent {

        @Valid
        private Pagination pagination = new Pagination();

        @NotNull
        private Long attestationTermIdWhenPatientSigns;

        @NotNull
        private Long attestationTermIdWhenProviderSigns;

        @NotNull
        private Long revocationTermIdWhenPatientRevokes;

        @NotNull
        private Long revocationTermIdWhenProviderRevokes;

        @NotNull
        @Valid
        private Publish publish;

        @Data
        public static class Publish {
            @NotNull
            private boolean enabled;

        }

        @Data
        public static class Pagination {
            @Min(1)
            @Max(500)
            private int defaultSize = 10;
            @Min(1)
            @Max(500)
            private int maxSize = 50;
        }
    }
}
