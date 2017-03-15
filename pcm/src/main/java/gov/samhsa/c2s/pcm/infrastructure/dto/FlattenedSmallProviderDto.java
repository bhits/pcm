package gov.samhsa.c2s.pcm.infrastructure.dto;

import lombok.Data;

@Data
public class FlattenedSmallProviderDto {
    private Long id;
    private String firstName;
    private String lastName;
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
}
