package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.Valid;

@Entity
@Table(indexes = @Index(columnList = "system,value", name = "purpose_identifier_idx", unique = true))
@Data
@Audited
public class Purpose implements I18nEnabled {
    @Id
    @GeneratedValue
    private Long id;

    @Embedded
    @Valid
    private Identifier identifier;

    private String display;

    private String description;

    @Override
    public String getIdAsString() {
        return longToString(id);
    }
}