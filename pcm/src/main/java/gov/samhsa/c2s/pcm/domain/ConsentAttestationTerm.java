package gov.samhsa.c2s.pcm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
@Data
@Audited
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentAttestationTerm {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Size(max = 20000)
    private String text;
}
