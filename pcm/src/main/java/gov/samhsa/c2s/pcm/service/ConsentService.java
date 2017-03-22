package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
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
}
