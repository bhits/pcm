package gov.samhsa.c2s.pcm.domain.valueobject;

import lombok.Data;

import javax.persistence.Embeddable;

@Embeddable
@Data
public class Address {
    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
