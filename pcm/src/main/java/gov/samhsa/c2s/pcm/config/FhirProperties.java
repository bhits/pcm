package gov.samhsa.c2s.pcm.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.pcm.fhir")
@Data
@Validated
public class FhirProperties {

    @NotNull
    @Valid
    private Ssn ssn;
    @NotNull
    @Valid
    private Npi npi;
    @NotNull
    @Valid
    private Pou pou;
    @NotNull
    @Valid
    private Mrn mrn;
    @NotNull
    private boolean patientReference;

    @Data
    public static class Identifier {
        @NotBlank
        private String system;

        @NotBlank
        private String oid;

        @NotBlank
        private String label;
    }

    @Data
    public static class Mrn extends Identifier {
    }

    @Data
    public static class Ssn extends Identifier {
    }

    @Data
    public static class Npi extends Identifier {
    }

    @Data
    public static class Pou extends Identifier {
    }
}
