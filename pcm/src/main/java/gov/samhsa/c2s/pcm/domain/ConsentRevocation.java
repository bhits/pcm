package gov.samhsa.c2s.pcm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Audited
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "consent")
@EqualsAndHashCode(exclude = "consent")
public class ConsentRevocation {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @NotNull
    @JsonIgnore
    private Consent consent;

    private Date revokedDate;

    private String revokedBy;

    private Boolean revokedByPatient;

    @ManyToOne
    @NotNull
    private ConsentRevocationTerm consentRevocationTerm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] consentRevocationPdf;
}
