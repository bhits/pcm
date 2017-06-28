package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentTermDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentTypeConfigurationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ConsentLookupRestController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/consentAttestationTerm")
    public ConsentTermDto getConsentAttestationTerm(@RequestParam Optional<Long> id) {
        return consentService.getConsentAttestationTerm(id);
    }

    @GetMapping("/consentRevocationTerm")
    public ConsentTermDto getConsentRevocationTerm(@RequestParam Optional<Long> id) {
        return consentService.getConsentRevocationTerm(id);
    }

    @GetMapping("/consents/consentTypeConfiguration")
    public ConsentTypeConfigurationDto getConsentTypeConfiguration() {
        return consentService.getConsentTypeConfiguration();
    }
}
