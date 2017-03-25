package gov.samhsa.c2s.pcm.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.pcm.fhir")
@Data
public class FhirProperties {

    private Ssn ssn;
    private Npi npi;
    private Pou pou;
    private Mrn mrn;
    private ConsentType consentType;
    private boolean keepExcludeList;

    @Data
    public static class Identifier {
        @NotNull
        private String system;

        @NotEmpty
        private String oid;

        @NotEmpty
        private String label;
    }

    @Data
    public static class ConsentType extends Identifier{
        private String code;
    }

    @Data
    public static class Mrn extends Identifier{ }

    @Data
    public static class Ssn extends Identifier{ }

    @Data
    public static class Npi extends Identifier{ }

    @Data
    public static class Pou extends Identifier{ }

}
