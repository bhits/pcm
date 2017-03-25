package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface ProviderService {
    @Transactional
    void saveProviders(Long patientId, Set<IdentifierDto> providerIdentifierDtos);

    @Transactional
    List<AbstractProviderDto> getProviders(Long patientId);

    @Transactional
    void deleteProvider(Long patientId, Long providerId);
}
