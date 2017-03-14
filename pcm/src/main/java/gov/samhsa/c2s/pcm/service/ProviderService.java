package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ProviderIdentifierDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProviderService {
    @Transactional
    void saveProviders(Long patientId, List<ProviderIdentifierDto> providerIdentifierDtos);

    @Transactional
    List<FlattenedSmallProviderDto> getProviders(Long patientId);
}
