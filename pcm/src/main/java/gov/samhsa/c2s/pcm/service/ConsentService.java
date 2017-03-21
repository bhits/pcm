package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConsentService {
    @Transactional
    List<ConsentDto> getConsents(Long patientId);

    @Transactional
    void saveConsent(Long patientId, ConsentDto consentDto);

    @Transactional
    void updateConsent(Long patientId, Long consentId, ConsentDto consentDto);


    @Transactional
    void attestConsent(Long patientId, Long consentId, ConsentAttestationDto consentAttestationDto);
}
