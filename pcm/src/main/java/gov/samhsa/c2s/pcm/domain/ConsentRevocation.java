package gov.samhsa.c2s.pcm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Audited
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentRevocation {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @NotNull
    private Consent consent;

    @ManyToOne
    @NotNull
    private ConsentRevocationTerm consentRevocationTerm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] consentRevocationPdf;
}
