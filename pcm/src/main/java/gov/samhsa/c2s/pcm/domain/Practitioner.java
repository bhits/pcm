package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
@Audited
public class Practitioner {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Provider provider;

    @ManyToOne
    private Consent consent;

    private String firstName;
    private String middleName;
    private String lastName;

    @Embedded
    private Address address;

    @ManyToOne
    private ConsentAttestation consentAttestation;
}
