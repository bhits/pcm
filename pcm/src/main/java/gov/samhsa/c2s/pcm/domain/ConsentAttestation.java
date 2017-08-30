package gov.samhsa.c2s.pcm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Audited
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "consent")
@EqualsAndHashCode(exclude = {"consent"})
public class ConsentAttestation {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @NotNull
    private Consent consent;

    private Date attestedDate;

    private String attestedBy;

    private Boolean attestedByPatient;

    @ManyToOne
    @NotNull
    private ConsentAttestationTerm consentAttestationTerm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] consentAttestationPdf;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] fhirConsent;

    @OneToMany(mappedBy = "consentAttestationFrom", cascade = CascadeType.ALL)
    @NotAudited
    private List<Practitioner> fromPractitioners = new ArrayList<>();

    @OneToMany(mappedBy = "consentAttestationFrom", cascade = CascadeType.ALL)
    @NotAudited
    private List<Organization> fromOrganizations = new ArrayList<>();

    @OneToMany(mappedBy = "consentAttestationTo", cascade = CascadeType.ALL)
    @NotAudited
    private List<Practitioner> toPractitioners = new ArrayList<>();

    @OneToMany(mappedBy = "consentAttestationTo", cascade = CascadeType.ALL)
    @NotAudited
    private List<Organization> toOrganizations = new ArrayList<>();
}
