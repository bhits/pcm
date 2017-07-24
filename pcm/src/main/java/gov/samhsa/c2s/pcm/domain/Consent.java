package gov.samhsa.c2s.pcm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.ScriptAssert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Where(clause = "deleted=0")
@EntityListeners(AuditingEntityListener.class)
@Audited
@ScriptAssert(
        lang = "javascript",
        alias = "_",
        script = "_.hasValidDates()",
        message = "consent end date must be after consent start date, consent start date must be start of today or future unless it is being revoked")
@Data
@ToString(exclude = "patient")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Consent {

    @Id
    @GeneratedValue
    private Long id;

    @CreatedDate
    private Date createdDate;

    private String createdBy;

    private Boolean createdByPatient;

    @LastModifiedDate
    private Date lastUpdatedDate;

    private String lastUpdatedBy;

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

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @NotNull
    private String consentReferenceId;

    @NotNull
    private boolean deleted = false;

    public void setConsentAttestation(ConsentAttestation consentAttestation) {
        setConsentStage(ConsentStage.SIGNED);
        this.consentAttestation = consentAttestation;
    }

    public void setConsentRevocation(ConsentRevocation consentRevocation) {
        setConsentStage(ConsentStage.REVOKED);
        this.consentRevocation = consentRevocation;
    }

    public boolean hasValidStartDate(LocalDateTime now) {
        // Enforce strict validation for start date if the consent is not getting deleted
        if (!ConsentStage.DELETED.equals(getConsentStage())) {
            // Enforce strict validation for start date if the consent is not getting revoked
            if (!ConsentStage.REVOKED.equals(getConsentStage())) {
                final LocalDateTime startOfToday = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
                final boolean validStartDate = getStartDate() != null && (startOfToday.isBefore(getStartDate()) || startOfToday.isEqual(getStartDate()));
                return validStartDate;
            }
        }
        final boolean validStartDate = getStartDate() != null;
        return validStartDate;
    }

    public boolean hasValidEndDate(LocalDateTime now) {
        final boolean validEndDate = getEndDate() != null && now.isBefore(getEndDate());
        return validEndDate;
    }

    public boolean hasValidDateRange() {
        final boolean startDateIsBeforeEndDate = getStartDate() != null && getEndDate() != null && getStartDate().isBefore(getEndDate());
        return startDateIsBeforeEndDate;
    }

    public boolean hasValidDates() {
        final LocalDateTime now = LocalDateTime.now();
        final boolean validDates = hasValidStartDate(now) && hasValidEndDate(now) && hasValidDateRange();
        return validDates;
    }
}
