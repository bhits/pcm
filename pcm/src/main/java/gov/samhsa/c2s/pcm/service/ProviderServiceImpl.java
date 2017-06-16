package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.config.PcmProperties;
import gov.samhsa.c2s.pcm.domain.Patient;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.ProviderRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import gov.samhsa.c2s.pcm.service.dto.PractitionerDto;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderTypeException;
import gov.samhsa.c2s.pcm.service.exception.PatientOrProviderNotFoundException;
import gov.samhsa.c2s.pcm.service.exception.PatientProviderNotFoundException;
import gov.samhsa.c2s.pcm.service.exception.ProviderIsAlreadyInUseException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    @Autowired
    private ModelMapper modelMapper;

    // TODO: refactor this static methods, so ConsentServiceImpl won't need to directly call these. The 'deletable' boolean can be potentially calculated by the Provider, Organization, Practitioner domain entities.
    public static boolean isProviderInUse(Set<Identifier> providerIdentifiersWithConsents, Provider provider) {
        return providerIdentifiersWithConsents.contains(provider.getIdentifier());
    }

    // TODO: refactor this static methods, so ConsentServiceImpl won't need to directly call these. The 'deletable' boolean can be potentially calculated by the Provider, Organization, Practitioner domain entities.
    public static boolean isProviderInUse(Set<Identifier> providerIdentifiersWithConsents, Set<IdentifierDto> identifiers) {
        return providerIdentifiersWithConsents.stream()
                .anyMatch(identifier -> identifiers.stream()
                        .anyMatch(dto -> identifier.getSystem().equals(dto.getSystem()) && identifier.getValue().equals(dto.getValue())));
    }

    // TODO: refactor this static methods, so ConsentServiceImpl won't need to directly call these. The 'deletable' boolean can be potentially calculated by the Provider, Organization, Practitioner domain entities.
    public static Set<Identifier> getProviderIdentifiersWithConsents(Patient patient) {
        return patient.getConsents().stream()
                .flatMap(consent -> Stream.concat(
                        consent.getFromProviders().stream().map(Provider::getIdentifier),
                        consent.getToProviders().stream().map(Provider::getIdentifier)))
                .collect(toSet());
    }

    @Override
    @Transactional
    public void saveProviders(String patientId, Set<IdentifierDto> providerIdentifierDtos) {
        // Assert provider identifier systems
        providerIdentifierDtos.stream()
                .map(IdentifierDto::getSystem)
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
    public List<AbstractProviderDto> getProviders(String patientId) {

        final Patient patient = patientRepository.saveAndGet(patientId);
        final Set<Identifier> providerIdentifiersWithConsents = getProviderIdentifiersWithConsents(patient);
        return patient.getProviders().stream()
                .map(provider -> {
                    final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                    flattenedSmallProvider.setId(provider.getId());
                    flattenedSmallProvider.setSystem(provider.getIdentifier().getSystem());
                    flattenedSmallProvider.setDeletable(!isProviderInUse(providerIdentifiersWithConsents, provider));
                    return flattenedSmallProvider;
                })
                .sorted(comparing(FlattenedSmallProviderDto::getId))
                .map(flattenedSmallProviderDto -> Optional.of(flattenedSmallProviderDto)
                        .filter(providerDto -> PlsService.ProviderType.INDIVIDUAL.equalsIgnoreCase(providerDto.getEntityTypeDisplayName()))
                        .map(providerDto -> modelMapper.map(providerDto, PractitionerDto.class))
                        .map(AbstractProviderDto.class::cast)
                        .orElseGet(() -> Optional.of(flattenedSmallProviderDto)
                                .filter(providerDto -> PlsService.ProviderType.ORGANIZATION.equalsIgnoreCase(providerDto.getEntityTypeDisplayName()))
                                .map(providerDto -> modelMapper.map(providerDto, OrganizationDto.class))
                                .orElseThrow(InvalidProviderTypeException::new)))
                .collect(toList());
    }

    @Override
    public void deleteProvider(String patientId, Long providerId) {
        final Patient patient = patientRepository
                .findOneByIdAndProvidersId(patientId, providerId)
                .orElseThrow(PatientOrProviderNotFoundException::new);
        final Provider provider = providerRepository.findOneById(providerId).orElseThrow(PatientProviderNotFoundException::new);
        final Set<Identifier> providerIdentifiersWithConsents = getProviderIdentifiersWithConsents(patient);

        if (isProviderInUse(providerIdentifiersWithConsents, provider)) {
            throw new ProviderIsAlreadyInUseException();
        } else {
            patient.getProviders().remove(provider);
            patientRepository.save(patient);
        }
    }
}
