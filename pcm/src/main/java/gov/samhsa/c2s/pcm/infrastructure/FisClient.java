package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientConsentDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("fis")
@Service
public interface FisClient {

    @RequestMapping(value = "/consents", method = RequestMethod.POST)
    String publishAndGetAttestedFhirConsent(
            @RequestBody PatientConsentDto patientConsentDto
            );

    @RequestMapping(value = "/consents", method = RequestMethod.PUT)
    String revokeAndGetRevokedFhirConsent(
            @RequestBody PatientConsentDto patientConsentDto
    );
}
