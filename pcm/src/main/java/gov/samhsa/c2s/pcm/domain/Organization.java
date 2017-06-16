package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Audited
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ScriptAssert(
        lang = "javascript",
        alias = "_",
        script = "(_.consentAttestationFrom != null &&  _.consentAttestationTo == null)||(_.consentAttestationFrom == null &&  _.consentAttestationTo != null)",
        message = "Organization must be assigned to a consent attestation")
public class Organization {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @NotNull
    private Provider provider;


    @NotBlank
    private String name;

    @Embedded
    @Valid
    private Address address;

    private String phoneNumber;

    @ManyToOne
    private ConsentAttestation consentAttestationFrom;

    @ManyToOne
    private ConsentAttestation consentAttestationTo;
}
