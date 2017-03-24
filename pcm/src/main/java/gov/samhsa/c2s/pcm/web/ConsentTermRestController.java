package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentTermDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ConsentTermRestController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/consentAttestationTerm")
    public ConsentTermDto getConsentAttestationTerm(@PathVariable Optional<Long> id) {
        return consentService.getConsentAttestationTerm(id);
    }

    @GetMapping("/consentRevocationTerm")
    public ConsentTermDto getConsentRevocationTerm(@PathVariable Optional<Long> id) {
        return consentService.getConsentRevocationTerm(id);
    }
}
