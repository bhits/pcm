package gov.samhsa.c2s.pcm.domain;

import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
@Data
@Audited
public class ConsentRevocationTerm {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Size(max = 20000)
    private String text;
}
