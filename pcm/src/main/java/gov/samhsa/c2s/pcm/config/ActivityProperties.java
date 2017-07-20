package gov.samhsa.c2s.pcm.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "c2s.pcm")
@Slf4j
@Data
public class ActivityProperties {
    @Autowired
    private ApplicationContext applicationContext;

    @NotNull
    @Valid
    public Activity activity;

    @Data
    public static class Activity {
        @NotNull
        public Pagination pagination;

        @NotNull
        public List<Sql> sqls;

        @Data
        public static class Pagination {
            @Min(1)
            private int defaultSize;
        }

        @Data
        public static class Sql {
            @NotNull
            @Min(1)
            public int index;
            @NotEmpty
            public String filePath;
        }
    }

    @PostConstruct
    private void checkDuplicateSqlSource() {
        boolean isUniqueSqlSource = activity.getSqls().stream()
                .allMatch(new HashSet<>()::add);

        if (!isUniqueSqlSource) {
            log.error("The index or file path configuration of activity sql source is unique.");
            SpringApplication.exit(applicationContext, () -> 2);
        }
    }
}
