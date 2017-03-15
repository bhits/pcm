package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.Valid;

@Entity
@Data
public class Purpose {
    @EmbeddedId
    @Valid
    private Identifier identifier;
    private String display;
}
