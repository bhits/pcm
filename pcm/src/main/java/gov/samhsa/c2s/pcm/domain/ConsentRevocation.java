package gov.samhsa.c2s.pcm.domain;

import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@Data
@Audited
public class ConsentRevocation {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Consent consent;

    @ManyToOne
    private ConsentRevocationTerm consentRevocationTerm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] consentRevocationPdf;
}
