package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ProviderIdentifierDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface ProviderService {
    @Transactional
    void saveProviders(Long patientId, Set<ProviderIdentifierDto> providerIdentifierDtos);

    @Transactional
    List<FlattenedSmallProviderDto> getProviders(Long patientId);

    @Transactional
    void deleteProvider(Long patientId, Long providerId);
}
