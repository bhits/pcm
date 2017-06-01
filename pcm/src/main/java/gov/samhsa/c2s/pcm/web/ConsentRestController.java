package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevocationDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.SensitivityCategoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patients/{patientId}")
public class ConsentRestController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/consents")
    public Page<DetailedConsentDto> getConsents(@PathVariable String patientId,
                                                @RequestParam(value = "purposeOfUse") Optional<String> purposeOfUse,
                                                @RequestParam(value = "fromProvider") Optional<Long> fromProvider,
                                                @RequestParam(value = "toProvider") Optional<Long> toProvider,
                                                @RequestParam Optional<Integer> page,
                                                @RequestParam Optional<Integer> size) {
        return consentService.getConsents(patientId, purposeOfUse, fromProvider, toProvider, page, size);
    }

    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveConsent(@PathVariable String patientId,
                            @Valid @RequestBody ConsentDto consentDto) {
        consentService.saveConsent(patientId, consentDto);
    }

    @DeleteMapping("/consents/{consentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsent(@PathVariable String patientId,
                              @PathVariable Long consentId) {
        consentService.deleteConsent(patientId, consentId);
    }

    @PutMapping("/consents/{consentId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateConsent(@PathVariable String patientId, @PathVariable Long consentId,
                              @Valid @RequestBody ConsentDto consentDto) {
        consentService.updateConsent(patientId, consentId, consentDto);
    }

    @PutMapping("/consents/{consentId}/attestation")
    @ResponseStatus(HttpStatus.OK)
    public void attestConsent(@PathVariable String patientId, @PathVariable Long consentId,
                              @Valid @RequestBody ConsentAttestationDto consentAttestationDto) {
        consentService.attestConsent(patientId, consentId, consentAttestationDto);
    }

    @PutMapping("/consents/{consentId}/revocation")
    @ResponseStatus(HttpStatus.OK)
    public void revokeConsent(@PathVariable String patientId, @PathVariable Long consentId,
                              @Valid @RequestBody ConsentRevocationDto consentRevocationDto) {
        consentService.revokeConsent(patientId, consentId, consentRevocationDto);
    }


    @GetMapping("/consents/{consentId}")
    public Object getConsent(@PathVariable String patientId, @PathVariable Long consentId,
                                        @RequestParam(required = false)  String format) {
        return consentService.getConsent(patientId, consentId, format);
    }

    @GetMapping("/consents/{consentId}/attestation")
    public Object getAttestedConsent(@PathVariable String patientId, @PathVariable Long consentId,
                             @RequestParam(required = false)  String format) {
        return consentService.getAttestedConsent(patientId, consentId, format);
    }

    @GetMapping("/consents/{consentId}/revocation")
    public Object getRevokedConsent(@PathVariable String patientId, @PathVariable Long consentId,
                                     @RequestParam(required = false)  String format) {
        return consentService.getRevokedConsent(patientId, consentId, format);
    }

    @GetMapping("/consents/{consentId}/shareSensitivityCategories")
    public List<SensitivityCategoryDto> getSharedSensitivityCategories(@PathVariable String patientId, @PathVariable Long consentId) {
        return consentService.getSharedSensitivityCategories(patientId, consentId);
    }
}

