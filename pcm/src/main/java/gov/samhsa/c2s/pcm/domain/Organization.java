package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotBlank;

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
public class Organization {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @NotNull
    private Provider provider;

    @ManyToOne
    @NotNull
    private Consent consent;

    @NotBlank
    private String name;

    @Embedded
    @Valid
    private Address address;

    @ManyToOne
    @NotNull
    private ConsentAttestation consentAttestation;
}
