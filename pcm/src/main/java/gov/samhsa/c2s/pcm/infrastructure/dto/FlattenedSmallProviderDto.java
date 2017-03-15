package gov.samhsa.c2s.pcm.infrastructure.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class FlattenedSmallProviderDto {
    private Long id;
    private String firstName;
    private String lastName;
    @NotBlank
    private String npi;
    private String middleName;
    private String organizationName;
    private String enumerationDate;
    private String lastUpdateDate;
    private String entityTypeDisplayName;
    private String entityTypeCode;
    private String businessPracticeLocationAddressFaxNumber;
    private String practiceLocationAddressTelephoneNumber;
    private String firstLinePracticeLocationAddress;
    private String secondLinePracticeLocationAddress;
    private String practiceLocationAddressCityName;
    private String practiceLocationAddressStateName;
    private String practiceLocationAddressPostalCode;
    private String practiceLocationAddressCountryCode;
    private String system;
}
