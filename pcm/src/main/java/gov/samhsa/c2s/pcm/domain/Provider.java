package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.Valid;

@Entity
@Table(indexes = @Index(columnList = "system,value", name = "provider_identifier_idx", unique = true))
@Audited
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Provider {
    @Id
    @GeneratedValue
    private Long id;

    @Embedded
    @Valid
    private Identifier identifier;
}