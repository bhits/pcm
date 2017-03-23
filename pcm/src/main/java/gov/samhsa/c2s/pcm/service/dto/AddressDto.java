package gov.samhsa.c2s.pcm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
