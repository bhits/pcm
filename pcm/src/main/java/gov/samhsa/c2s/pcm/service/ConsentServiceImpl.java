package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.domain.ConsentAttestation;
import gov.samhsa.c2s.pcm.domain.ConsentAttestationRepository;
import gov.samhsa.c2s.pcm.domain.ConsentAttestationTermRepository;
import gov.samhsa.c2s.pcm.domain.ConsentRepository;
import gov.samhsa.c2s.pcm.domain.Organization;
import gov.samhsa.c2s.pcm.domain.OrganizationRepository;
import gov.samhsa.c2s.pcm.domain.Patient;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.Practitioner;
import gov.samhsa.c2s.pcm.domain.PractitionerRepository;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.ProviderRepository;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.domain.SensitivityCategory;
import gov.samhsa.c2s.pcm.domain.SensitivityCategoryRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.infrastructure.PhrService;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderException;
import gov.samhsa.c2s.pcm.service.exception.InvalidPurposeException;
import gov.samhsa.c2s.pcm.service.exception.InvalidSensitivityCategoryException;
import gov.samhsa.c2s.pcm.service.exception.PatientOrSavedConsentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
public class ConsentServiceImpl implements ConsentService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PurposeRepository purposeRepository;

    @Autowired
    private PractitionerRepository practitionerRepository;

    @Autowired
    private OrganizationRepository organizationRepository;



    @Autowired
    private ConsentAttestationRepository consentAttestationRepository;


    @Autowired
    private ConsentAttestationTermRepository consentAttestationTermRepository;


    @Autowired
    private SensitivityCategoryRepository sensitivityCategoryRepository;

    @Autowired
    private PhrService phrService;

    @Autowired
    private PlsService plsService;

    @Override
    @Transactional
    public List<ConsentDto> getConsents(Long patientId) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        return patient.getConsents().stream()
                .map(consent -> new ConsentDto())
                .collect(toList());
    }

    @Override
    public void saveConsent(Long patientId, ConsentDto consentDto) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        final List<Provider> fromProviders = consentDto.getFromProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<Provider> toProviders = consentDto.getToProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<SensitivityCategory> shareSensitivityCategories = consentDto.getShareSensitivityCategories().getIdentifiers().stream()
                .map(toSensitivityCategory())
                .collect(toList());
        final List<Purpose> sharePurposes = consentDto.getSharePurposes().getIdentifiers().stream()
                .map(toPurpose())
                .collect(toList());
        final LocalDate startDate = consentDto.getStartDate();
        final LocalDate endDate = consentDto.getEndDate();
        final Consent consent = Consent.builder()
                .patient(patient)
                .fromProviders(fromProviders)
                .toProviders(toProviders)
                .shareSensitivityCategories(shareSensitivityCategories)
                .sharePurposes(sharePurposes)
                .startDate(startDate)
                .endDate(endDate)
                .consentStage(ConsentStage.SAVED)
                .build();
        consentRepository.save(consent);
        patient.getConsents().add(consent);
        patientRepository.save(patient);
    }

    @Override
    public void deleteConsent(Long patientId, Long consentId) {
        final Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(consentId, patientId).orElseThrow(PatientOrSavedConsentNotFoundException::new);
        Assert.isNull(consent.getConsentAttestation(), "Cannot delete an attested consent");
        Assert.isNull(consent.getConsentRevocation(), "Cannot delete an revoked consent");
        Assert.isTrue(ConsentStage.SAVED.equals(consent.getConsentStage()), "Cannot delete a consent that is not in 'SAVED' stage");
        consentRepository.delete(consent);
    }

    private Function<IdentifierDto, Purpose> toPurpose() {
        return identifier -> purposeRepository.findOneByIdentifierSystemAndIdentifierValue(identifier.getSystem(), identifier.getValue()).orElseThrow(InvalidPurposeException::new);
    }

    private Function<IdentifierDto, SensitivityCategory> toSensitivityCategory() {
        return identifier -> sensitivityCategoryRepository.findOneByIdentifierSystemAndIdentifierValue(identifier.getSystem(), identifier.getValue()).orElseThrow(InvalidSensitivityCategoryException::new);
    }

    private Function<IdentifierDto, Provider> toProvider(Patient patient) {
        return identifier -> patient.getProviders().stream()
                .filter(provider -> provider.getIdentifier().getSystem().equals(identifier.getSystem()) && provider.getIdentifier().getValue().equals(identifier.getValue()))
                .findAny().orElseThrow(InvalidProviderException::new);
    }

    @Override
    public void attestConsent(Long patientId, Long consentId, ConsentAttestationDto consentAttestationDto) {
        if (consentAttestationDto.getAcceptTerms().equals(Boolean.TRUE)) {

            //get patient
            final Patient patient = patientRepository.saveAndGet(patientId);

            Consent consent = consentRepository.findOne(consentId);

            //get getFromProviders
            final List<FlattenedSmallProviderDto> fromProviderDtos = consent.getFromProviders().stream().map(provider -> {
                final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                flattenedSmallProvider.setSystem(provider.getIdentifier().getSystem());
                return flattenedSmallProvider;
            }).collect(toList());

            //save fromPractitioners
            List<Practitioner> fromPractitioners = fromProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals("Individual"))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToPractitioner(flattenedSmallProviderDto, patient, consent))
                    .collect(toList());


            //save fromOrganizations
            List<Organization> fromOrganizations = fromProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals("Organization"))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToOrganization(flattenedSmallProviderDto, patient, consent))
                    .collect(toList());


            //get getToProviders
            final List<FlattenedSmallProviderDto> toProviderDtos = consent.getToProviders().stream().map(provider -> {
                final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                flattenedSmallProvider.setSystem(provider.getIdentifier().getSystem());
                return flattenedSmallProvider;
            }).collect(toList());

            //save toPractitioners
            List<Practitioner> toPractitioners = toProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals("Individual"))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToPractitioner(flattenedSmallProviderDto, patient, consent ))
                    .collect(toList());

            //save toOrganizations
            List<Organization> toOrganizations = toProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals("Organization"))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToOrganization(flattenedSmallProviderDto, patient, consent))
                    .collect(toList());


            //build atteststation consent
            final ConsentAttestation consentAttestation = ConsentAttestation.builder()
                    .fromOrganizations(fromOrganizations)
                    .fromPractitioners(fromPractitioners)
                    .toOrganizations(toOrganizations)
                    .toPractitioners(toPractitioners)
                    .consentAttestationTerm(consentAttestationTermRepository.findOne(Long.valueOf(1)))
                    .consent(consent)
                    .build();

            fromOrganizations.stream().forEach(organization -> organization.setConsentAttestation(consentAttestation));
            toOrganizations.stream().forEach(organization -> organization.setConsentAttestation(consentAttestation));
            fromPractitioners.stream().forEach(practitioner -> practitioner.setConsentAttestation(consentAttestation));
            toPractitioners.stream().forEach(practitioner -> practitioner.setConsentAttestation(consentAttestation));


            //update consent
            consent.setConsentStage(ConsentStage.SIGNED);
            consent.setConsentAttestation(consentAttestation);

            consentRepository.save(consent);

        }
    }

    private Practitioner mapFlattenedSmallProviderToPractitioner(FlattenedSmallProviderDto flattenedSmallProviderDto, Patient patient, Consent consent) {
        return Practitioner.builder().lastName(flattenedSmallProviderDto.getLastName())
                .firstName(flattenedSmallProviderDto.getFirstName())
                .middleName(flattenedSmallProviderDto.getMiddleName())
                .address(Address.builder().line1(flattenedSmallProviderDto.getFirstLinePracticeLocationAddress())
                        .line2(flattenedSmallProviderDto.getSecondLinePracticeLocationAddress())
                        .city(flattenedSmallProviderDto.getPracticeLocationAddressCityName())
                        .state(flattenedSmallProviderDto.getPracticeLocationAddressStateName())
                        .postalCode(flattenedSmallProviderDto.getPracticeLocationAddressPostalCode())
                        .country(flattenedSmallProviderDto.getPracticeLocationAddressCountryCode())
                        .build())
               .provider(findProvider(flattenedSmallProviderDto.getSystem(),flattenedSmallProviderDto.getNpi(),patient))
                .consent(consent)
                .build();
    }


    private Organization mapFlattenedSmallProviderToOrganization(FlattenedSmallProviderDto flattenedSmallProviderDto, Patient patient,Consent consent) {
        return Organization.builder().name(flattenedSmallProviderDto.getOrganizationName())
                .address(Address.builder().line1(flattenedSmallProviderDto.getFirstLinePracticeLocationAddress())
                        .line2(flattenedSmallProviderDto.getSecondLinePracticeLocationAddress())
                        .city(flattenedSmallProviderDto.getPracticeLocationAddressCityName())
                        .state(flattenedSmallProviderDto.getPracticeLocationAddressStateName())
                        .postalCode(flattenedSmallProviderDto.getPracticeLocationAddressPostalCode())
                        .country(flattenedSmallProviderDto.getPracticeLocationAddressCountryCode())
                        .build())
             .provider(findProvider(flattenedSmallProviderDto.getSystem(),flattenedSmallProviderDto.getNpi(),patient))
                .consent(consent)
                .build();

    }

    private Provider findProvider(String system, String value, Patient patient) {
        return patient.getProviders().stream()
                .filter(provider -> provider.getIdentifier().getSystem().equals(system) && provider.getIdentifier().getValue().equals(value))
                .findAny().orElseThrow(InvalidProviderException::new);

    }


    @Override
    public void updateConsent(Long patientId, Long consentId, ConsentDto consentDto) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        Consent consent = consentRepository.findOne(consentId);
        final List<Provider> fromProviders = consentDto.getFromProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<Provider> toProviders = consentDto.getToProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<SensitivityCategory> shareSensitivityCategories = consentDto.getShareSensitivityCategories().getIdentifiers().stream()
                .map(toSensitivityCategory())
                .collect(toList());
        final List<Purpose> sharePurposes = consentDto.getSharePurposes().getIdentifiers().stream()
                .map(toPurpose())
                .collect(toList());

        consent.setStartDate(consentDto.getStartDate());
        consent.setEndDate(consentDto.getEndDate());
        consent.setFromProviders(fromProviders);
        consent.setToProviders(toProviders);
        consent.setShareSensitivityCategories(shareSensitivityCategories);
        consent.setSharePurposes(sharePurposes);

        consentRepository.save(consent);
    }

}
