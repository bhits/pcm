package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.config.PcmProperties;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.domain.ConsentAttestation;
import gov.samhsa.c2s.pcm.domain.ConsentAttestationTerm;
import gov.samhsa.c2s.pcm.domain.ConsentAttestationTermRepository;
import gov.samhsa.c2s.pcm.domain.ConsentRepository;
import gov.samhsa.c2s.pcm.domain.ConsentRevocation;
import gov.samhsa.c2s.pcm.domain.ConsentRevocationTerm;
import gov.samhsa.c2s.pcm.domain.ConsentRevocationTermRepository;
import gov.samhsa.c2s.pcm.domain.Organization;
import gov.samhsa.c2s.pcm.domain.Patient;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.Practitioner;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.domain.SensitivityCategory;
import gov.samhsa.c2s.pcm.domain.SensitivityCategoryRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.domain.valueobject.Identifier;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.pdf.ConsentPdfGenerator;
import gov.samhsa.c2s.pcm.infrastructure.pdf.ConsentRevocationPdfGenerator;
import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevocationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentTermDto;
import gov.samhsa.c2s.pcm.service.dto.ContentDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifiersDto;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import gov.samhsa.c2s.pcm.service.dto.PractitionerDto;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import gov.samhsa.c2s.pcm.service.dto.SensitivityCategoryDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.pcm.service.exception.BadRequestException;
import gov.samhsa.c2s.pcm.service.exception.ConsentNotFoundException;
import gov.samhsa.c2s.pcm.service.exception.DuplicateConsentException;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderException;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderTypeException;
import gov.samhsa.c2s.pcm.service.exception.InvalidPurposeException;
import gov.samhsa.c2s.pcm.service.exception.PatientOrSavedConsentNotFoundException;
import gov.samhsa.c2s.pcm.service.fhir.FhirConsentService;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
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

    @Autowired
    public ConsentServiceImpl(ConsentAttestationTermRepository consentAttestationTermRepository, ConsentPdfGenerator consentPdfGenerator, ConsentRepository consentRepository, ConsentRevocationPdfGenerator consentRevocationPdfGenerator, ConsentRevocationTermRepository consentRevocationTermRepository, ModelMapper modelMapper, PatientRepository patientRepository, PcmProperties pcmProperties, UmsService umsService, PlsService plsService, PurposeRepository purposeRepository, SensitivityCategoryRepository sensitivityCategoryRepository, FhirConsentService fhirConsentService) {
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
                    oneConsent.getSharePurposes().stream()
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

        // Assert consent is not conflicting with an existing consent
        final Set<Identifier> fromProviderIdentifiers = fromProviders.stream().map(Provider::getIdentifier).collect(toSet());
        final Set<Identifier> toProviderIdentifiers = toProviders.stream().map(Provider::getIdentifier).collect(toSet());
        final Set<Identifier> sharePurposeIdentifiers = sharePurposes.stream().map(Purpose::getIdentifier).collect(toSet());
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
                                consent.getSharePurposes().stream().map(Purpose::getIdentifier)
                                        .anyMatch(sharePurposeIdentifiers::contains));
        if (duplicate) {
            throw new DuplicateConsentException();
        }

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

        final List<SensitivityCategoryDto> shareSensitivityCategories = consent.getShareSensitivityCategories().stream()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .collect(toList());
        final List<PurposeDto> sharePurposes = consent.getSharePurposes().stream()
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

        return DetailedConsentDto.builder()
                .id(consent.getId())
                .fromProviders(fromProviders)
                .toProviders(toProviders)
                .shareSensitivityCategories(shareSensitivityCategories)
                .sharePurposes(sharePurposes)
                .startDate(consent.getStartDate())
                .endDate(consent.getEndDate())
                .consentStage(consent.getConsentStage())
                .consentReferenceId(consent.getConsentReferenceId())
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
        Consent consent = consentRepository.findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(consentId, patientId).orElseThrow(ConsentNotFoundException::new);

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
        IdentifiersDto shareSensitivityCategory = IdentifiersDto.of(consent.getShareSensitivityCategories().stream().distinct()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .map(sensitivityCategoryDto -> sensitivityCategoryDto.getIdentifier())
                .collect(Collectors.toSet()));

        IdentifiersDto sharePurposs = IdentifiersDto.of(consent.getSharePurposes().stream().distinct()
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
                .shareSensitivityCategories(shareSensitivityCategory)
                .sharePurposes(sharePurposs)
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

        return consent.getShareSensitivityCategories().stream()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DetailedConsentDto searchConsent(XacmlRequestDto xacmlRequestDto) {

        final Consent searchConsent = consentRepository.findOneByPatientIdAndFromProvidersIdentifierValueAndToProvidersIdentifierValueAndSharePurposesIdentifierValueAndStartDateBeforeAndEndDateAfterAndConsentAttestationNotNull(
                xacmlRequestDto.getPatientId().getExtension(),
                xacmlRequestDto.getIntermediaryNpi(),
                xacmlRequestDto.getRecipientNpi(),
                xacmlRequestDto.getPurposeOfUse().getPurposeFhir(),
                LocalDate.now(),
                LocalDate.now()).orElseThrow(ConsentNotFoundException::new);
        return mapToDetailedConsentDto(searchConsent);
    }


}
