package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Audited
public class Consent {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Patient patient;

    @ManyToMany
    private List<Provider> fromProviders = new ArrayList<>();

    @ManyToMany
    private List<Provider> toProviders = new ArrayList<>();

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] savedPdf;

    @OneToOne(mappedBy = "consent")
    private ConsentAttestation consentAttestation;

    @OneToOne(mappedBy = "consent")
    private ConsentRevocation consentRevocation;

    @ManyToMany
    private List<SensitivityCategory> sensitivityCategories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    private ConsentStage consentStage = ConsentStage.SAVED;

    public void setConsentAttestation(ConsentAttestation consentAttestation) {
        setConsentStage(ConsentStage.SIGNED);
        this.consentAttestation = consentAttestation;
    }

    public void setConsentRevocation(ConsentRevocation consentRevocation) {
        setConsentStage(ConsentStage.REVOKED);
        this.consentRevocation = consentRevocation;
    }
}
