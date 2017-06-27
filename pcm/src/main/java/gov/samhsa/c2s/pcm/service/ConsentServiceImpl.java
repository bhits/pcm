package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.config.PcmProperties;
import gov.samhsa.c2s.pcm.domain.*;
import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.pdf.ConsentPdfGenerator;
import gov.samhsa.c2s.pcm.infrastructure.pdf.ConsentRevocationPdfGenerator;
import gov.samhsa.c2s.pcm.service.dto.*;
import gov.samhsa.c2s.pcm.service.exception.*;
import gov.samhsa.c2s.pcm.service.fhir.FhirConsentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@Slf4j
public class ConsentServiceImpl implements ConsentService {

    private final ConsentAttestationTermRepository consentAttestationTermRepository;
    private final ConsentPdfGenerator consentPdfGenerator;
    private final ConsentRepository consentRepository;
    private final ConsentRevocationPdfGenerator consentRevocationPdfGenerator;
    private final ConsentRevocationTermRepository consentRevocationTermRepository;
    private final ModelMapper modelMapper;
    private final PatientRepository patientRepository;
    private final PcmProperties pcmProperties;
    private final UmsService umsService;
    private final PlsService plsService;
    private final PurposeRepository purposeRepository;
    private final SensitivityCategoryRepository sensitivityCategoryRepository;
    private final FhirConsentService fhirConsentService;
    private final ConsentTypeConfigurationRepository consentTypeConfigurationRepository;
    private final ApplicationContext applicationContext;

    @Autowired
    public ConsentServiceImpl(ConsentAttestationTermRepository consentAttestationTermRepository, ConsentPdfGenerator consentPdfGenerator,
                              ConsentRepository consentRepository, ConsentRevocationPdfGenerator consentRevocationPdfGenerator,
                              ConsentRevocationTermRepository consentRevocationTermRepository, ModelMapper modelMapper,
                              PatientRepository patientRepository, PcmProperties pcmProperties, UmsService umsService, PlsService plsService,
                              PurposeRepository purposeRepository, SensitivityCategoryRepository sensitivityCategoryRepository,
                              FhirConsentService fhirConsentService, ConsentTypeConfigurationRepository consentTypeConfigurationRepository,
                              ApplicationContext applicationContext) {
        this.consentAttestationTermRepository = consentAttestationTermRepository;
        this.consentPdfGenerator = consentPdfGenerator;
        this.consentRepository = consentRepository;
        this.consentRevocationPdfGenerator = consentRevocationPdfGenerator;
        this.consentRevocationTermRepository = consentRevocationTermRepository;
        this.modelMapper = modelMapper;
        this.patientRepository = patientRepository;
        this.pcmProperties = pcmProperties;
        this.umsService = umsService;
        this.plsService = plsService;
        this.purposeRepository = purposeRepository;
        this.sensitivityCategoryRepository = sensitivityCategoryRepository;
        this.fhirConsentService = fhirConsentService;
        this.consentTypeConfigurationRepository = consentTypeConfigurationRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DetailedConsentDto> getConsents(String patientId, Optional<Long> purposeOfUse,
                                                Optional<Long> fromProvider, Optional<Long> toProvider,
                                                Optional<Integer> page, Optional<Integer> size) {
        final PageRequest pageRequest = new PageRequest(page.filter(p -> p >= 0).orElse(0),
                size.filter(s -> s > 0 && s <= pcmProperties.getConsent().getPagination().getMaxSize()).orElse(pcmProperties.getConsent().getPagination().getDefaultSize()));
        final Page<Consent> consentsPage = consentRepository.findAllByPatientId(patientId, pageRequest);
        List<Consent> consents = consentsPage.getContent();

        if (purposeOfUse.isPresent()) {
            consents = consents.stream().filter(oneConsent ->
                    oneConsent.getPurposes().stream()
                            .anyMatch(onePurpose ->
                                    onePurpose.getId().equals(purposeOfUse.get()))
            ).collect(toList());
        }

        if (fromProvider.isPresent()) {
            consents = consents.stream().filter(oneConsent ->
                    oneConsent.getFromProviders().stream()
                            .anyMatch(oneProvider ->
                                    oneProvider.getId().equals(fromProvider.get()))
            ).collect(toList());
        }

        if (toProvider.isPresent()) {
            consents = consents.stream().filter(oneConsent ->
                    oneConsent.getToProviders().stream()
                            .anyMatch(oneProvider ->
                                    oneProvider.getId().equals(toProvider.get()))
            ).collect(toList());
        }

        final List<DetailedConsentDto> detailedConsentDtos = consents.stream()
                .map(this::mapToDetailedConsentDto)
                .collect(toList());
        return new PageImpl<>(detailedConsentDtos, pageRequest, consentsPage.getTotalElements());
    }

    @Override
    @Transactional
    public void saveConsent(String patientId, ConsentDto consentDto, Optional<String> createdBy, Optional<Boolean> createdByPatient) {

        ConsentTypeConfiguration consentTypeConfiguration =  getConsentShareSensitivityCategory();

        final Patient patient = patientRepository.saveAndGet(patientId);
        final List<Provider> fromProviders = consentDto.getFromProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<Provider> toProviders = consentDto.getToProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<SensitivityCategory> sensitivityCategories = getSensitivityCategoriesByConsentType(consentTypeConfiguration, consentDto);

        final List<Purpose> purposes = consentDto.getPurposes().getIdentifiers().stream()
                .map(toPurpose())
                .collect(toList());

        // Assert consent is not conflicting with an existing consent
        final Set<Identifier> fromProviderIdentifiers = fromProviders.stream().map(Provider::getIdentifier).collect(toSet());
        final Set<Identifier> toProviderIdentifiers = toProviders.stream().map(Provider::getIdentifier).collect(toSet());
        final Set<Identifier> purposeIdentifiers = purposes.stream().map(Purpose::getIdentifier).collect(toSet());
        final boolean duplicate = patient.getConsents().stream()
                // find any consent that is not in 'REVOKED' stage
                .filter(consent -> !ConsentStage.REVOKED.equals(consent.getConsentStage()))
                .anyMatch(consent ->
                        // contains any of the from providers and
                        consent.getFromProviders().stream()
                                .map(Provider::getIdentifier)
                                .anyMatch(fromProviderIdentifiers::contains) &&
                                // contains any of the to providers and
                                consent.getToProviders().stream()
                                        .map(Provider::getIdentifier)
                                        .anyMatch(toProviderIdentifiers::contains) &&
                                // the date overlaps and
                                (!(consent.getStartDate().isAfter(consentDto.getEndDate()) || consentDto.getStartDate().isAfter(consent.getEndDate()))) &&
                                // contains any of the share purposes
                                consent.getPurposes().stream().map(Purpose::getIdentifier)
                                        .anyMatch(purposeIdentifiers::contains));
        if (duplicate) {
            throw new DuplicateConsentException();
        }

        final LocalDate startDate = consentDto.getStartDate();
        final LocalDate endDate = consentDto.getEndDate();
        final Consent consent = Consent.builder()
                .patient(patient)
                .fromProviders(fromProviders)
                .toProviders(toProviders)
                .sensitivityCategories(sensitivityCategories)
                .purposes(purposes)
                .startDate(startDate)
                .endDate(endDate)
                .consentTypeConfiguration(consentTypeConfiguration)
                .consentStage(ConsentStage.SAVED)
                .consentReferenceId(RandomStringUtils
                .randomAlphanumeric(10))
                .createdBy(createdBy.orElse(null))
                .createdByPatient(createdByPatient.orElse(null))
                .lastUpdatedBy(createdBy.orElse(null))
                .build();

        //generate pdf
        PatientDto patientDto = umsService.getPatientProfile(patientId);

        consent.setSavedPdf(consentPdfGenerator.generate42CfrPart2Pdf(consent, patientDto, false, null, consentAttestationTermRepository.findOne(Long.valueOf(1)).getText()));

        consentRepository.save(consent);
        patient.getConsents().add(consent);
        patientRepository.save(patient);
    }
    List<SensitivityCategory>  getSensitivityCategoriesByConsentType(ConsentTypeConfiguration consentTypeConfiguration, ConsentDto consentDto){

        List<SensitivityCategory> sensistivityCategories = null;

       if(consentTypeConfiguration.isShareConsentTypeConfigured()){ // SHARE
           sensistivityCategories = consentDto.getSensitivityCategories().getIdentifiers().stream()
                   .map(toSensitivityCategory())
                   .collect(toList());
        }else  if(!consentTypeConfiguration.isShareConsentTypeConfigured()){ // DO NOT SHARE
           sensistivityCategories = sensitivityCategoryRepository.findAll();
           List<SensitivityCategory> selectedSensistivityCategories = consentDto.getSensitivityCategories().getIdentifiers().stream()
                   .map(toSensitivityCategory())
                   .collect(toList());
           sensistivityCategories.removeAll(selectedSensistivityCategories);

       }
        return sensistivityCategories;
    }


    private void shutdownApplication(){
        SpringApplication.exit(applicationContext);
    }

    @PostConstruct
    public void setDefaultConsentShareSensitivityCategories(){
        boolean shareSensitivityCategoriesEnabled = pcmProperties.getConsent().getShareConsentTypeConfigured().isEnabled();
        Optional<ConsentTypeConfiguration> optionalShareSensitivityCategories = consentTypeConfigurationRepository.findOneById(Long.valueOf(1));
        if(!optionalShareSensitivityCategories.isPresent()){
            // Add to DB
            ConsentTypeConfiguration consentTypeConfiguration = new ConsentTypeConfiguration();
            consentTypeConfiguration.setShareConsentTypeConfigured(shareSensitivityCategoriesEnabled);
            consentTypeConfigurationRepository.save(consentTypeConfiguration);
        }else if(optionalShareSensitivityCategories.isPresent() &&
                shareSensitivityCategoriesEnabled != optionalShareSensitivityCategories.get().isShareConsentTypeConfigured() ){

            // Allow change if there is no consent in DB of

            // Shut down application
            log.error("Shutting down application. Cannot override SHARE/NOT SHARE DB configuration");
            shutdownApplication();
        }
    }

    private ConsentTypeConfiguration getConsentShareSensitivityCategory(){
        Optional<ConsentTypeConfiguration> optionalShareSensitivityCategories = null;

        optionalShareSensitivityCategories = consentTypeConfigurationRepository.findOneById(Long.valueOf(1));
        if(!optionalShareSensitivityCategories.isPresent()){
            boolean shareSensitivityCategoriesEnabled = pcmProperties.getConsent().getShareConsentTypeConfigured().isEnabled();
            ConsentTypeConfiguration consentTypeConfiguration = new ConsentTypeConfiguration();
            consentTypeConfiguration.setShareConsentTypeConfigured(shareSensitivityCategoriesEnabled);
            return consentTypeConfiguration;
        }
        return optionalShareSensitivityCategories.get();
    }


    @Override
    @Transactional
    public void deleteConsent(String patientId, Long consentId, Optional<String> lastUpdatedBy) {
        Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(consentId, patientId).orElseThrow(PatientOrSavedConsentNotFoundException::new);
        Assert.isNull(consent.getConsentAttestation(), "Cannot delete an attested consent");
        Assert.isNull(consent.getConsentRevocation(), "Cannot delete an revoked consent");
        Assert.isTrue(ConsentStage.SAVED.equals(consent.getConsentStage()), "Cannot delete a consent that is not in 'SAVED' stage");
        consent.setLastUpdatedBy(lastUpdatedBy.orElse(null));
        /*
          An entity when deleted will only contain the id of the entity and no data in the audit table.
          Therefore, saving the consent before deleting to track the lastUpdatedBy info
          TODO: Find a better way
         */
        consentRepository.save(consent);
        consentRepository.delete(consent);
    }

    private DetailedConsentDto mapToDetailedConsentDto(Consent consent) {
        //Consent Attestation Info
        Date attestedDate = null;
        String attestedBy = null;
        Boolean attestedByPatient = null;

        //Consent Revocation Info
        Date revokedDate = null;
        String revokedBy = null;
        Boolean revokedByPatient = null;

        final List<SensitivityCategoryDto> shareSensitivityCategories = consent.getSensitivityCategories().stream()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .collect(toList());
        final List<PurposeDto> sharePurposes = consent.getPurposes().stream()
                .map(purpose -> modelMapper.map(purpose, PurposeDto.class))
                .collect(toList());
        final Set<Identifier> providerIdentifiersWithConsents = ProviderServiceImpl.getProviderIdentifiersWithConsents(consent.getPatient());

        final List<AbstractProviderDto> fromProviders = Optional.ofNullable(consent.getConsentAttestation())
                .map(consentAttestation -> Stream.concat(
                        consentAttestation.getFromOrganizations().stream()
                                .map(organization -> modelMapper.map(organization, OrganizationDto.class))
                                .map(AbstractProviderDto.class::cast),
                        consentAttestation.getFromPractitioners().stream()
                                .map(practitioner -> modelMapper.map(practitioner, PractitionerDto.class))
                                .map(AbstractProviderDto.class::cast)))
                .orElseGet(() -> consent.getFromProviders().stream().map(this::toAbstractProviderDto))
                .peek(abstractProviderDto -> abstractProviderDto.setDeletable(!ProviderServiceImpl.isProviderInUse(providerIdentifiersWithConsents, abstractProviderDto.getIdentifiers())))
                .collect(toList());

        final List<AbstractProviderDto> toProviders = Optional.ofNullable(consent.getConsentAttestation())
                .map(consentAttestation -> Stream.concat(
                        consentAttestation.getToOrganizations().stream()
                                .map(organization -> modelMapper.map(organization, OrganizationDto.class))
                                .map(AbstractProviderDto.class::cast),
                        consentAttestation.getToPractitioners().stream()
                                .map(practitioner -> modelMapper.map(practitioner, PractitionerDto.class))
                                .map(AbstractProviderDto.class::cast)))
                .orElseGet(() -> consent.getToProviders().stream().map(this::toAbstractProviderDto))
                .peek(abstractProviderDto -> abstractProviderDto.setDeletable(!ProviderServiceImpl.isProviderInUse(providerIdentifiersWithConsents, abstractProviderDto.getIdentifiers())))
                .collect(toList());

        //A revoked consent also contains attestation info
        if(consent.getConsentStage().name().equalsIgnoreCase(ConsentStage.REVOKED.toString()) ||
                consent.getConsentStage().name().equalsIgnoreCase(ConsentStage.SIGNED.toString())){
            ConsentAttestation attestedConsent = consent.getConsentAttestation();
            attestedDate = attestedConsent.getAttestedDate();
            attestedBy = attestedConsent.getAttestedBy();
            attestedByPatient = attestedConsent.getAttestedByPatient();
        }

        if(consent.getConsentStage().name().equalsIgnoreCase(ConsentStage.REVOKED.toString())){
            ConsentRevocation revokedConsent = consent.getConsentRevocation();
            revokedDate = revokedConsent.getRevokedDate();
            revokedBy = revokedConsent.getRevokedBy();
            revokedByPatient = revokedConsent.getRevokedByPatient();
        }

        return DetailedConsentDto.builder()
                .id(consent.getId())
                .fromProviders(fromProviders)
                .toProviders(toProviders)
                .sensitivityCategories(shareSensitivityCategories)
                .purposes(sharePurposes)
                .startDate(consent.getStartDate())
                .endDate(consent.getEndDate())
                .consentStage(consent.getConsentStage())
                .consentReferenceId(consent.getConsentReferenceId())
                .createdDate(consent.getCreatedDate())
                .createdBy(consent.getCreatedBy())
                .createdByPatient(consent.getCreatedByPatient())
                .lastUpdatedBy(consent.getLastUpdatedBy())
                .lastUpdatedDate(consent.getLastUpdatedDate())
                .attestedDate(attestedDate)
                .attestedBy(attestedBy)
                .attestedByPatient(attestedByPatient)
                .revokedDate(revokedDate)
                .revokedBy(revokedBy)
                .revokedByPatient(revokedByPatient)
                .build();
    }

    private AbstractProviderDto toAbstractProviderDto(Provider provider) {
        final FlattenedSmallProviderDto flattenedSmallProviderDto = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
        flattenedSmallProviderDto.setSystem(
                Optional.ofNullable(flattenedSmallProviderDto.getSystem())
                        .orElse(provider.getIdentifier().getSystem()));

        return Optional.of(flattenedSmallProviderDto)
                .filter(providerDto -> PlsService.ProviderType.INDIVIDUAL.equalsIgnoreCase(providerDto.getEntityTypeDisplayName()))
                .map(providerDto -> modelMapper.map(providerDto, PractitionerDto.class))
                .map(practitioner -> setIdAndDeletableFalseAndReturn(practitioner, provider))
                .map(AbstractProviderDto.class::cast)
                .orElseGet(() -> Optional.of(flattenedSmallProviderDto)
                        .filter(providerDto -> PlsService.ProviderType.ORGANIZATION.equalsIgnoreCase(providerDto.getEntityTypeDisplayName()))
                        .map(providerDto -> modelMapper.map(providerDto, OrganizationDto.class))
                        .map(organization -> setIdAndDeletableFalseAndReturn(organization, provider))
                        .orElseThrow(InvalidProviderTypeException::new));
    }

    private AbstractProviderDto setIdAndDeletableFalseAndReturn(AbstractProviderDto providerDto, Provider provider) {
        providerDto.setId(provider.getId());
        providerDto.setDeletable(Boolean.FALSE);
        return providerDto;
    }

    private Function<IdentifierDto, Purpose> toPurpose() {
        return identifier -> purposeRepository.findOneByIdentifierSystemAndIdentifierValue(identifier.getSystem(), identifier.getValue()).orElseThrow(InvalidPurposeException::new);
    }

    private Function<IdentifierDto, SensitivityCategory> toSensitivityCategory() {
        return identifier -> sensitivityCategoryRepository.saveAndGet(identifier.getSystem(), identifier.getValue());
    }

    private Function<IdentifierDto, Provider> toProvider(Patient patient) {
        return identifier -> patient.getProviders().stream()
                .filter(provider -> provider.getIdentifier().getSystem().equals(identifier.getSystem()) && provider.getIdentifier().getValue().equals(identifier.getValue()))
                .findAny().orElseThrow(InvalidProviderException::new);
    }

    @Override
    @Transactional
    public void attestConsent(String patientId, Long consentId, ConsentAttestationDto consentAttestationDto, Optional<String> attestedBy, Optional<Boolean> attestedByPatient) {
        //get patient
        final Patient patient = patientRepository.saveAndGet(patientId);

        Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(consentId, patientId).orElseThrow(ConsentNotFoundException::new);

        if (consentAttestationDto.isAcceptTerms()) {

            //get getFromProviders
            final List<FlattenedSmallProviderDto> fromProviderDtos = consent.getFromProviders().stream().map(provider -> {
                final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                flattenedSmallProvider.setSystem(provider.getIdentifier().getSystem());
                return flattenedSmallProvider;
            }).collect(toList());

            //save fromPractitioners
            List<Practitioner> fromPractitioners = fromProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals(PlsService.ProviderType.INDIVIDUAL))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToPractitioner(flattenedSmallProviderDto, patient))
                    .collect(toList());


            //save fromOrganizations
            List<Organization> fromOrganizations = fromProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals(PlsService.ProviderType.ORGANIZATION))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToOrganization(flattenedSmallProviderDto, patient))
                    .collect(toList());


            //get getToProviders
            final List<FlattenedSmallProviderDto> toProviderDtos = consent.getToProviders().stream().map(provider -> {
                final FlattenedSmallProviderDto flattenedSmallProvider = plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER);
                flattenedSmallProvider.setSystem(provider.getIdentifier().getSystem());
                return flattenedSmallProvider;
            }).collect(toList());

            //save toPractitioners
            List<Practitioner> toPractitioners = toProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals(PlsService.ProviderType.INDIVIDUAL))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToPractitioner(flattenedSmallProviderDto, patient))
                    .collect(toList());

            //save toOrganizations
            List<Organization> toOrganizations = toProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals(PlsService.ProviderType.ORGANIZATION))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToOrganization(flattenedSmallProviderDto, patient))
                    .collect(toList());

            ConsentAttestationTerm consentAttestationTerm = consentAttestationTermRepository.findOne(Long.valueOf(1));

            //build atteststation consent
            final ConsentAttestation consentAttestation = ConsentAttestation.builder()
                    .fromOrganizations(fromOrganizations)
                    .fromPractitioners(fromPractitioners)
                    .toOrganizations(toOrganizations)
                    .toPractitioners(toPractitioners)
                    .consentAttestationTerm(consentAttestationTerm)
                    .consent(consent)
                    .attestedBy(attestedBy.orElse(null))
                    .attestedByPatient(attestedByPatient.orElse(null))
                    .build();

            fromOrganizations.forEach(organization -> organization.setConsentAttestationFrom(consentAttestation));
            toOrganizations.forEach(organization -> organization.setConsentAttestationTo(consentAttestation));
            fromPractitioners.forEach(practitioner -> practitioner.setConsentAttestationFrom(consentAttestation));
            toPractitioners.forEach(practitioner -> practitioner.setConsentAttestationTo(consentAttestation));

            //update consent
            consent.setConsentStage(ConsentStage.SIGNED);
            consent.setConsentAttestation(consentAttestation);

            PatientDto patientDto = umsService.getPatientProfile(patientId);

            //generate consent pdf
            consentAttestation.setConsentAttestationPdf(consentPdfGenerator.generate42CfrPart2Pdf(consent, patientDto, true, new Date(), consentAttestationTerm.getText()));

            // generate FHIR Consent and publish consent to FHIR server if enabled
            if (pcmProperties.getConsent().getPublish().isEnabled()) {
                consentAttestation.setFhirConsent(fhirConsentService.getAttestedFhirConsent(consent, patientDto));
            }
            consentRepository.save(consent);

        } else throw new BadRequestException();
    }

    private Practitioner mapFlattenedSmallProviderToPractitioner(FlattenedSmallProviderDto flattenedSmallProviderDto, Patient patient) {
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
                .phoneNumber(flattenedSmallProviderDto.getPracticeLocationAddressTelephoneNumber())
                .provider(findProvider(flattenedSmallProviderDto.getSystem(), flattenedSmallProviderDto.getNpi(), patient))
                .build();
    }


    private Organization mapFlattenedSmallProviderToOrganization(FlattenedSmallProviderDto flattenedSmallProviderDto, Patient patient) {
        return Organization.builder().name(flattenedSmallProviderDto.getOrganizationName())
                .address(Address.builder().line1(flattenedSmallProviderDto.getFirstLinePracticeLocationAddress())
                        .line2(flattenedSmallProviderDto.getSecondLinePracticeLocationAddress())
                        .city(flattenedSmallProviderDto.getPracticeLocationAddressCityName())
                        .state(flattenedSmallProviderDto.getPracticeLocationAddressStateName())
                        .postalCode(flattenedSmallProviderDto.getPracticeLocationAddressPostalCode())
                        .country(flattenedSmallProviderDto.getPracticeLocationAddressCountryCode())
                        .build())
                .phoneNumber(flattenedSmallProviderDto.getPracticeLocationAddressTelephoneNumber())
                .provider(findProvider(flattenedSmallProviderDto.getSystem(), flattenedSmallProviderDto.getNpi(), patient))
                .build();

    }

    private Provider findProvider(String system, String value, Patient patient) {
        return patient.getProviders().stream()
                .filter(provider -> provider.getIdentifier().getSystem().equals(system) && provider.getIdentifier().getValue().equals(value))
                .findAny().orElseThrow(InvalidProviderException::new);

    }


    @Override
    @Transactional
    public void updateConsent(String patientId, Long consentId, ConsentDto consentDto, Optional<String> lastUpdatedBy) {
        final Patient patient = patientRepository.saveAndGet(patientId);
        ConsentTypeConfiguration consentTypeConfiguration =  getConsentShareSensitivityCategory();
        Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(consentId, patientId).orElseThrow(ConsentNotFoundException::new);

        final List<Provider> fromProviders = consentDto.getFromProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<Provider> toProviders = consentDto.getToProviders().getIdentifiers().stream()
                .map(toProvider(patient))
                .collect(toList());
        final List<SensitivityCategory> sensitivityCategories = getSensitivityCategoriesByConsentType( getConsentShareSensitivityCategory(), consentDto);

        final List<Purpose> sharePurposes = consentDto.getPurposes().getIdentifiers().stream()
                .map(toPurpose())
                .collect(toList());

        consent.setStartDate(consentDto.getStartDate());
        consent.setEndDate(consentDto.getEndDate());
        consent.setFromProviders(fromProviders);
        consent.setToProviders(toProviders);
        consent.setSensitivityCategories(sensitivityCategories);
        consent.setPurposes(sharePurposes);
        consent.setConsentTypeConfiguration(consentTypeConfiguration);
        consent.setLastUpdatedBy(lastUpdatedBy.orElse(null));

        //generate pdf
        PatientDto patientDto = umsService.getPatientProfile(patientId);
        consent.setSavedPdf(consentPdfGenerator.generate42CfrPart2Pdf(consent, patientDto, false, new Date(), consentAttestationTermRepository.findOne(Long.valueOf(1)).getText()));

        consentRepository.save(consent);

    }


    @Override
    @Transactional
    public void revokeConsent(String patientId, Long consentId, ConsentRevocationDto consentRevocationDto, Optional<String> revokedBy, Optional<Boolean> revokedByPatient) {

        Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNotNullAndConsentRevocationIsNull(consentId, patientId).orElseThrow(ConsentNotFoundException::new);

        ConsentRevocationTerm consentRevocationTerm = consentRevocationTermRepository.findOne(Long.valueOf(1));

        if (consentRevocationDto.isAcceptTerms()) {

            //build consentRevocation
            final ConsentRevocation consentRevocation = ConsentRevocation.builder()
                    .consentRevocationTerm(consentRevocationTerm)
                    .consent(consent)
                    .revokedBy(revokedBy.orElse(null))
                    .revokedByPatient(revokedByPatient.orElse(null))
                    .build();

            //update consent
            consent.setConsentStage(ConsentStage.REVOKED);


            PatientDto patientDto = umsService.getPatientProfile(patientId);
            consentRevocation.setConsentRevocationPdf(consentRevocationPdfGenerator.generateConsentRevocationPdf(consent, patientDto, new Date(), consentRevocationTerm.getText()));

            consent.setConsentRevocation(consentRevocation);

            //revoke consent on FHIR server if enabled
            if (pcmProperties.getConsent().getPublish().isEnabled()) {
                consent.getConsentAttestation().setFhirConsent(fhirConsentService.getRevokedFhirConsent(consent, patientDto));
            }
            consentRepository.save(consent);
        } else throw new BadRequestException();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getConsent(String patientId, Long consentId, String format) {
        final Consent consent = consentRepository.findOneByIdAndPatientId(consentId, patientId).orElseThrow(ConsentNotFoundException::new);
        if (format != null && format.equals("pdf")) {
            if (consent.getConsentStage().equals(ConsentStage.SAVED))
                return new ContentDto("application/pdf", consent.getSavedPdf());
            else
                throw new BadRequestException("Please download attested consent.");
        }
        if (format != null && format.equals("detailedConsent") && consent.getConsentStage().equals(ConsentStage.SAVED)) {
            return mapToDetailedConsentDto(consent);
        }

        return toConsentDto(consent);
    }

    @Override
    public ShareSensitivityCategoriesDto getShareSensitivityCategoriesConfig() {
        ShareSensitivityCategoriesDto  sensitivityCategoriesDto = new ShareSensitivityCategoriesDto();
        ConsentTypeConfiguration consentTypeConfiguration =    consentTypeConfigurationRepository.findOneById(Long.valueOf(1)).get();
        sensitivityCategoriesDto.setShareSensitivityCategoriesEnabled(consentTypeConfiguration.isShareConsentTypeConfigured());
        return sensitivityCategoriesDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getAttestedConsent(String patientId, Long consentId, String format) {
        final Consent consent = consentRepository.findOneByIdAndPatientId(consentId, patientId).orElseThrow(ConsentNotFoundException::new);
        if (format != null && format.equals("pdf") && (!consent.getConsentStage().equals(ConsentStage.SAVED))) {
            return new ContentDto("application/pdf", consent.getConsentAttestation().getConsentAttestationPdf());
        } else
            return mapToDetailedConsentDto(consent);

    }


    @Override
    @Transactional(readOnly = true)
    public Object getRevokedConsent(String patientId, Long consentId, String format) {
        final Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNotNullAndConsentRevocationIsNotNull(consentId, patientId).orElseThrow(ConsentNotFoundException::new);
        if (format != null && format.equals("pdf")) {
            return new ContentDto("application/pdf", consent.getConsentRevocation().getConsentRevocationPdf());
        } else
            return mapToDetailedConsentDto(consent);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentTermDto getConsentAttestationTerm(Optional<Long> id) {
        final Long termId = id.filter(i -> i != 1L).orElse(1L);
        ConsentAttestationTerm consentAttestationTerm = consentAttestationTermRepository.findOne(termId);
        Assert.notNull(consentAttestationTerm, "Consent attestation term cannot be found");
        return modelMapper.map(consentAttestationTerm, ConsentTermDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentTermDto getConsentRevocationTerm(Optional<Long> id) {
        final Long termId = id.filter(i -> i != 1L).orElse(1L);
        ConsentRevocationTerm consentRevocationTerm = consentRevocationTermRepository.findOne(termId);
        Assert.notNull(consentRevocationTerm, "Consent revocation term cannot be found");
        return modelMapper.map(consentRevocationTerm, ConsentTermDto.class);
    }

    private ConsentDto toConsentDto(Consent consent) {
        IdentifiersDto shareSensitivityCategory = IdentifiersDto.of(consent.getSensitivityCategories().stream().distinct()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .map(sensitivityCategoryDto -> sensitivityCategoryDto.getIdentifier())
                .collect(Collectors.toSet()));

        IdentifiersDto sharePurposs = IdentifiersDto.of(consent.getPurposes().stream().distinct()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .map(sensitivityCategoryDto -> sensitivityCategoryDto.getIdentifier())
                .collect(Collectors.toSet()));

        IdentifiersDto fromProviders = IdentifiersDto.of(consent.getFromProviders().stream().distinct()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .map(sensitivityCategoryDto -> sensitivityCategoryDto.getIdentifier())
                .collect(Collectors.toSet()));

        IdentifiersDto toProviders = IdentifiersDto.of(consent.getToProviders().stream().distinct()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .map(sensitivityCategoryDto -> sensitivityCategoryDto.getIdentifier())
                .collect(Collectors.toSet()));


        return ConsentDto.builder()
                .endDate(consent.getEndDate())
                .startDate(consent.getStartDate())
                .sensitivityCategories(shareSensitivityCategory)
                .purposes(sharePurposs)
                .shareSensitivityCategoriesEnabled(consent.getConsentTypeConfiguration().isShareConsentTypeConfigured())
                .fromProviders(fromProviders)
                .toProviders(toProviders)
                .id(consent.getId())
                .consentReferenceId(consent.getConsentReferenceId())
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public List<SensitivityCategoryDto> getSharedSensitivityCategories(String patientId, Long consentId) {
        final Consent consent = consentRepository.findOneByIdAndPatientId(consentId, patientId).orElseThrow(ConsentNotFoundException::new);

        return consent.getSensitivityCategories().stream()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DetailedConsentDto searchConsent(XacmlRequestDto xacmlRequestDto) {
        log.debug("Invoking searchConsent Method" + xacmlRequestDto);
        final Consent searchConsent = consentRepository.findOneByPatientIdAndFromProvidersIdentifierValueAndToProvidersIdentifierValueAndPurposesIdentifierValueAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndConsentAttestationNotNull(
                xacmlRequestDto.getPatientId().getExtension(),
                xacmlRequestDto.getIntermediaryNpi(),
                xacmlRequestDto.getRecipientNpi(),
                xacmlRequestDto.getPurposeOfUse().getPurposeFhir(),
                LocalDate.now(),
                LocalDate.now()).orElseThrow(ConsentNotFoundException::new);
        log.debug("consent search found" + searchConsent);
        return mapToDetailedConsentDto(searchConsent);
    }


}
