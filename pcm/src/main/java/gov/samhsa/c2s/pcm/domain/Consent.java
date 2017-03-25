package gov.samhsa.c2s.pcm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.samhsa.c2s.common.validator.constraint.PresentOrFuture;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@ScriptAssert(
        lang = "javascript",
        alias = "_",
        script = "_.startDate != null && _.endDate != null && _.startDate < _.endDate",
        message = "consent end date must be after consent start date")
@Data
@ToString(exclude = "patient")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Consent {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Patient patient;

    @ManyToMany
    private List<Provider> fromProviders = new ArrayList<>();

    @ManyToMany
    private List<Provider> toProviders = new ArrayList<>();

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] savedPdf;

    @OneToOne(mappedBy = "consent", cascade = CascadeType.ALL)
    private ConsentAttestation consentAttestation;

    @OneToOne(mappedBy = "consent", cascade = CascadeType.ALL)
    private ConsentRevocation consentRevocation;

    @ManyToMany
    private List<SensitivityCategory> shareSensitivityCategories = new ArrayList<>();

    @ManyToMany
    private List<Purpose> sharePurposes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    private ConsentStage consentStage = ConsentStage.SAVED;

    @PresentOrFuture
    private LocalDate startDate;

    @Future
    private LocalDate endDate;

    @NotNull
    private String consentReferenceId;

    public void setConsentAttestation(ConsentAttestation consentAttestation) {
        setConsentStage(ConsentStage.SIGNED);
        this.consentAttestation = consentAttestation;
    }

    public void setConsentRevocation(ConsentRevocation consentRevocation) {
        setConsentStage(ConsentStage.REVOKED);
        this.consentRevocation = consentRevocation;
    }
}
