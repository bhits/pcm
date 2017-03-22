package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevocationDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ConsentService {
    @Transactional
    Page<DetailedConsentDto> getConsents(Long patientId, Optional<Integer> page, Optional<Integer> size);

    @Transactional
    void saveConsent(Long patientId, ConsentDto consentDto);

    @Transactional
    void deleteConsent(Long patientId, Long consentId);

    @Transactional
    void updateConsent(Long patientId, Long consentId, ConsentDto consentDto);

    @Transactional
    void attestConsent(Long patientId, Long consentId, ConsentAttestationDto consentAttestationDto);

    @Transactional
    void revokeConsent(Long patientId, Long consentId, ConsentRevocationDto consentRevocationDto);
}
