package gov.samhsa.c2s.pcm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Audited
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareSensitivityCategories {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private boolean shareSensitivityCategoriesEnabled;
}
