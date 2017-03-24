package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.*;
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

    @Transactional(readOnly = true)
    Object getConsent(Long patientId, Long consentId, String format);

    @Transactional(readOnly = true)
    Object getAttestedConsent(Long patientId, Long consentId, String format);

    @Transactional(readOnly = true)
    Object getRevokedConsent(Long patientId, Long consentId, String format);

    @Transactional(readOnly = true)
    ConsentTermDto getConsentAttestationTerm(Optional<Long> id);

    @Transactional(readOnly = true)
    ConsentTermDto getConsentRevocationTerm(Optional<Long> id);
}
