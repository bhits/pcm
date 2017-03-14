package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Patient;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.ProviderRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ProviderIdentifierDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ProviderServiceImpl implements ProviderService {

    public static final String FHIR_US_NPI_SYSTEM = "http://hl7.org/fhir/sid/us-npi";

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PlsService plsService;

    @Override
    @Transactional
    public void saveProviders(Long patientId, List<ProviderIdentifierDto> providerIdentifierDtos) {
        final Patient patient = patientRepository.saveAndGet(patientId);

        final List<FlattenedSmallProviderDto> providerDtos = providerIdentifierDtos.stream()
                .map(ProviderIdentifierDto::getValue)
                .map(npi -> plsService.getFlattenedSmallProvider(npi, PlsService.Projection.FLATTEN_SMALL_PROVIDER))
                .collect(toList());

        final List<Provider> providers =
                providerDtos.stream().map(FlattenedSmallProviderDto::getNpi)
                        .distinct()
                        .map(npi -> Provider.builder()
                                .identifier(Identifier.builder()
                                        .value(npi)
                                        .system(FHIR_US_NPI_SYSTEM)
                                        .build())
                                .build())
                        .collect(toList());
        providerRepository.save(providers);
        patient.getProviders().addAll(providers);
        patientRepository.save(patient);
    }

    @Override
    @Transactional
    public List<FlattenedSmallProviderDto> getProviders(Long patientId) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        return patient.getProviders().stream()
                .map(provider -> plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER))
                .collect(toList());
    }
}
