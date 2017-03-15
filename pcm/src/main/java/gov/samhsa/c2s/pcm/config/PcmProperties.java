package gov.samhsa.c2s.pcm.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "c2s.pcm")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PcmProperties {

    @NotEmpty
    private List<String> supportedProviderSystems = new ArrayList<>();
}
