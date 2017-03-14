package gov.samhsa.c2s.pcm.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Identifier implements Serializable {
    @NotEmpty
    private String system;
    @NotEmpty
    private String value;
}
