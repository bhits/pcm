package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.config.PcmProperties;
import gov.samhsa.c2s.pcm.domain.Patient;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.ProviderRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ProviderIdentifierDto;
import gov.samhsa.c2s.pcm.service.exception.PatientOrProviderNotFoundException;
import gov.samhsa.c2s.pcm.service.exception.PatientProviderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class ProviderServiceImpl implements ProviderService {

    @Autowired
    private PcmProperties pcmProperties;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PlsService plsService;

    @Override
    @Transactional
    public void saveProviders(Long patientId, Set<ProviderIdentifierDto> providerIdentifierDtos) {
        // Assert provider identifier systems
        providerIdentifierDtos.stream()
                .map(ProviderIdentifierDto::getSystem)
                .forEach(system -> Assert.isTrue(pcmProperties.getSupportedProviderSystems().contains(system), "Invalid Provider System"));

        // Assert provider identifier values
        providerIdentifierDtos.stream()
                .map(identifier -> {
                    final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(identifier.getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                    flattenedSmallProvider.setSystem(identifier.getSystem());
                    return flattenedSmallProvider;
                })
                .forEach(flattenedSmallProvider -> Assert.notNull(flattenedSmallProvider, "Invalid Provider Selection"));

        final Patient patient = patientRepository.saveAndGet(patientId);

        // Add new providers to PCM
        final List<Provider> providersToAdd = providerIdentifierDtos.stream()
                .filter(identifier -> providerRepository.notExistsByIdentifierSystemAndIdentifierValue(identifier.getSystem(), identifier.getValue()))
                .map(identifier -> Provider.builder()
                        .identifier(Identifier.builder()
                                .value(identifier.getValue())
                                .system(identifier.getSystem())
                                .build())
                        .build())
                .collect(toList());
        providerRepository.save(providersToAdd);

        // Add new providers to Patient
        final List<Provider> providersToAddToPatient = providerIdentifierDtos.stream()
                .map(identifier -> providerRepository.findOneByIdentifierSystemAndIdentifierValue(identifier.getSystem(), identifier.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(provider -> patientRepository.notExistsByIdAndProvidersId(patientId, provider.getId()))
                .collect(toList());
        patient.getProviders().addAll(providersToAddToPatient);
        patientRepository.save(patient);
    }

    @Override
    @Transactional
    public List<FlattenedSmallProviderDto> getProviders(Long patientId) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        return patient.getProviders().stream()
                .map(provider -> {
                    final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                    flattenedSmallProvider.setId(provider.getId());
                    flattenedSmallProvider.setSystem(provider.getIdentifier().getSystem());
                    return flattenedSmallProvider;
                })
                .collect(toList());
    }

    @Override
    public void deleteProvider(Long patientId, Long providerId) {
        final Patient patient = patientRepository
                .findOneByIdAndProvidersId(patientId, providerId)
                .orElseThrow(PatientOrProviderNotFoundException::new);
        final Provider provider = providerRepository.findOneById(providerId).orElseThrow(PatientProviderNotFoundException::new);
        patient.getProviders().remove(provider);
        patientRepository.save(patient);
    }
}
