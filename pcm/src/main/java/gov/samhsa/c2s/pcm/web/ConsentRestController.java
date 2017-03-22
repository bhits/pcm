package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevocationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/patients/{patientId}")
public class ConsentRestController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/consents")
    public List<ConsentDto> getConsents(@PathVariable Long patientId) {
        log.info("Patient ID: {}", patientId);
        throw new NotImplementedException();
    }

    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveConsent(@PathVariable Long patientId,
                            @Valid @RequestBody ConsentDto consentDto) {
        consentService.saveConsent(patientId, consentDto);
    }

    @DeleteMapping("/consents/{consentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsent(@PathVariable Long patientId,
                              @PathVariable Long consentId) {
        consentService.deleteConsent(patientId, consentId);
    }

    @PutMapping("/consents/{consentId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateConsent(@PathVariable Long patientId, @PathVariable Long consentId,
                              @Valid @RequestBody ConsentDto consentDto) {
        consentService.updateConsent(patientId, consentId, consentDto);
    }

    @PutMapping("/consents/{consentId}/attestation")
    @ResponseStatus(HttpStatus.OK)
    public void attestConsent(@PathVariable Long patientId, @PathVariable Long consentId,
                              @Valid @RequestBody ConsentAttestationDto consentAttestationDto) {
        consentService.attestConsent(patientId, consentId, consentAttestationDto);
    }

    @PutMapping("/consents/{consentId}/revocation")
    @ResponseStatus(HttpStatus.OK)
    public void revokeConsent(@PathVariable Long patientId, @PathVariable Long consentId,
                              @Valid @RequestBody ConsentRevocationDto consentRevocationDto) {
            consentService.revokeConsent(patientId, consentId, consentRevocationDto);
    }

}
