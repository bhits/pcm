package gov.samhsa.c2s.pcm.domain;

import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
@Data
@Audited
public class ConsentAttestation {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Consent consent;

    @ManyToOne
    private ConsentAttestationTerm consentAttestationTerm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] consentAttestationPdf;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] fhirConsent;

    @OneToMany(mappedBy = "consentAttestation")
    @NotAudited
    private List<Practitioner> fromPractitioners;

    @OneToMany(mappedBy = "consentAttestation")
    @NotAudited
    private List<Organization> fromOrganizations;

    @OneToMany(mappedBy = "consentAttestation")
    @NotAudited
    private List<Practitioner> toPractitioners;

    @OneToMany(mappedBy = "consentAttestation")
    @NotAudited
    private List<Organization> toOrganizations;

}
