package gov.samhsa.c2s.pcm.domain.valueobject;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class Identifier implements Serializable {
    @NotEmpty
    private String system;
    @NotEmpty
    private String value;
}
