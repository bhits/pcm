package gov.samhsa.c2s.pcm.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "c2s.pcm")
@Slf4j
@Data
@Validated
public class ActivityProperties {

    @NotNull
    @NotEmpty
    @Valid
    public List<Activity> activities;

    @Data
    public static class Activity {
        @NotBlank
        public String type;

        @NotNull
        @Valid
        public Pagination pagination;

        @NotNull
        @Valid
        public SortBy sortBy;

        @NotNull
        @Valid
        public Sql sql;

        @Data
        public static class Pagination {
            @Min(1)
            @Max(500)
            private int defaultSize;
        }

        @Data
        public static class SortBy {
            @NotNull
            private Sort.Direction direction;
            @NotBlank
            private String property;
        }

        @Data
        public static class Sql {
            @NotBlank
            public String filePath;
        }
    }
}
