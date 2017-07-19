package gov.samhsa.c2s.pcm.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "c2s.pcm")
@Data
public class ActivityProperties {
    @NotNull
    @Valid
    public Activity activity;

    @Data
    public static class Activity {
        @NotNull
        public Pagination pagination;

        @NotNull
        public Sql sql;

        @Data
        public static class Pagination {
            @Min(1)
            private int defaultSize;
        }

        @Data
        public static class Sql {
            @NotEmpty
            public String filePath;
        }
    }
}
