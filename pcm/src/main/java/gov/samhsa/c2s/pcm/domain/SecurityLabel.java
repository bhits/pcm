package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
@Data
@Audited
public class SecurityLabel {
    @EmbeddedId
    private Identifier coding;
}
