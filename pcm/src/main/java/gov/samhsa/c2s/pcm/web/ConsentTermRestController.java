package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationTermDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ConsentTermRestController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/consentAttestationTerm")
    public ConsentAttestationTermDto getConsents(@RequestParam Optional<Long> id) {
        return consentService.getConsentAttestationTerm(id);
    }
}
