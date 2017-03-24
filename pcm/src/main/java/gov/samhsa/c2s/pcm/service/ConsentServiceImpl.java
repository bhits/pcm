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
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.pdf.ConsentPdfGenerator;
import gov.samhsa.c2s.pcm.infrastructure.pdf.ConsentRevocationPdfGenerator;
import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevocationDto;
import gov.samhsa.c2s.pcm.service.dto.ContentDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifiersDto;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import gov.samhsa.c2s.pcm.service.dto.PractitionerDto;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import gov.samhsa.c2s.pcm.service.dto.SensitivityCategoryDto;
import gov.samhsa.c2s.pcm.service.exception.BadRequestException;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderException;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderTypeException;
import gov.samhsa.c2s.pcm.service.exception.InvalidPurposeException;
import gov.samhsa.c2s.pcm.service.exception.InvalidSensitivityCategoryException;
import gov.samhsa.c2s.pcm.service.exception.PatientOrSavedConsentNotFoundException;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class ConsentServiceImpl implements ConsentService {

    @Autowired
    private ConsentAttestationTermRepository consentAttestationTermRepository;
    @Autowired
    private ConsentPdfGenerator consentPdfGenerator;
    @Autowired
    private ConsentRepository consentRepository;
    @Autowired
    private ConsentRevocationPdfGenerator consentRevocationPdfGenerator;
    @Autowired
    private ConsentRevocationTermRepository consentRevocationTermRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PcmProperties pcmProperties;
    @Autowired
    private PhrService phrService;
    @Autowired
    private PlsService plsService;
    @Autowired
    private ProviderRepository providerRepository;
    @Autowired
    private PurposeRepository purposeRepository;
    @Autowired
    private SensitivityCategoryRepository sensitivityCategoryRepository;

    @Override
    @Transactional
    public Page<DetailedConsentDto> getConsents(Long patientId, Optional<Integer> page, Optional<Integer> size) {
        final PageRequest pageRequest = new PageRequest(page.filter(p -> p >= 0).orElse(0),
                size.filter(s -> s > 0 && s <= pcmProperties.getConsent().getPagination().getMaxSize()).orElse(pcmProperties.getConsent().getPagination().getDefaultSize()));
        final Page<Consent> consentsPage = consentRepository.findAllByPatientId(patientId, pageRequest);
        final List<Consent> consents = consentsPage.getContent();
        final List<DetailedConsentDto> detailedConsentDtos = consents.stream()
                .map(this::mapToDetailedConsentDto)
                .collect(toList());
        Page<DetailedConsentDto> newPage = new PageImpl<>(detailedConsentDtos, pageRequest, consentsPage.getTotalElements());
        return newPage;
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
                .consentReferenceId(RandomStringUtils
                        .randomAlphanumeric(10))
                .build();

        //generate pdf
        PatientDto patientDto = phrService.getPatientProfile();

        consent.setSavedPdf(consentPdfGenerator.generate42CfrPart2Pdf(consent, patientDto, false, null, consentAttestationTermRepository.findOne(Long.valueOf(1)).getText()));

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

    private DetailedConsentDto mapToDetailedConsentDto(Consent consent) {

        final List<SensitivityCategoryDto> shareSensitivityCategories = consent.getShareSensitivityCategories().stream()
                .map(sensitivityCategory -> modelMapper.map(sensitivityCategory, SensitivityCategoryDto.class))
                .collect(toList());
        final List<PurposeDto> sharePurposes = consent.getSharePurposes().stream()
                .map(purpose -> modelMapper.map(purpose, PurposeDto.class))
                .collect(toList());

        final List<AbstractProviderDto> fromProviders = Optional.ofNullable(consent.getConsentAttestation())
                .map(consentAttestation -> Stream.concat(
                        consentAttestation.getFromOrganizations().stream()
                                .map(organization -> modelMapper.map(organization, OrganizationDto.class))
                                .map(AbstractProviderDto.class::cast),
                        consentAttestation.getFromPractitioners().stream()
                                .map(practitioner -> modelMapper.map(practitioner, PractitionerDto.class))
                                .map(AbstractProviderDto.class::cast)))
                .orElseGet(() -> consent.getFromProviders().stream().map(this::toAbstractProviderDto))
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
        return identifier -> sensitivityCategoryRepository.findOneByIdentifierSystemAndIdentifierValue(identifier.getSystem(), identifier.getValue()).orElseThrow(InvalidSensitivityCategoryException::new);
    }

    private Function<IdentifierDto, Provider> toProvider(Patient patient) {
        return identifier -> patient.getProviders().stream()
                .filter(provider -> provider.getIdentifier().getSystem().equals(identifier.getSystem()) && provider.getIdentifier().getValue().equals(identifier.getValue()))
                .findAny().orElseThrow(InvalidProviderException::new);
    }

    @Override
    public void attestConsent(Long patientId, Long consentId, ConsentAttestationDto consentAttestationDto) {
        //get patient
        final Patient patient = patientRepository.saveAndGet(patientId);

        Consent consent = consentRepository.findOne(consentId);

        if (consentAttestationDto.isAcceptTerms() && consent.getConsentStage().equals(ConsentStage.SAVED)) {

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
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToPractitioner(flattenedSmallProviderDto, patient, consent))
                    .collect(toList());

            //save toOrganizations
            List<Organization> toOrganizations = toProviderDtos.stream()
                    .filter(flattenedSmallProviderDto -> flattenedSmallProviderDto.getEntityTypeDisplayName().equals("Organization"))
                    .map(flattenedSmallProviderDto -> mapFlattenedSmallProviderToOrganization(flattenedSmallProviderDto, patient, consent))
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
                    .build();

            fromOrganizations.stream().forEach(organization -> organization.setConsentAttestation(consentAttestation));
            toOrganizations.stream().forEach(organization -> organization.setConsentAttestation(consentAttestation));
            fromPractitioners.stream().forEach(practitioner -> practitioner.setConsentAttestation(consentAttestation));
            toPractitioners.stream().forEach(practitioner -> practitioner.setConsentAttestation(consentAttestation));

            //update consent
            consent.setConsentStage(ConsentStage.SIGNED);
            consent.setConsentAttestation(consentAttestation);

            PatientDto patientDto = phrService.getPatientProfile();

            //generate consent pdf
            consentAttestation.setConsentAttestationPdf(consentPdfGenerator.generate42CfrPart2Pdf(consent, patientDto, true, new Date(), consentAttestationTerm.getText()));

            consentRepository.save(consent);

        } else throw new BadRequestException();
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
                .phoneNumber(flattenedSmallProviderDto.getPracticeLocationAddressTelephoneNumber())
                .provider(findProvider(flattenedSmallProviderDto.getSystem(), flattenedSmallProviderDto.getNpi(), patient))
                .consent(consent)
                .build();
    }


    private Organization mapFlattenedSmallProviderToOrganization(FlattenedSmallProviderDto flattenedSmallProviderDto, Patient patient, Consent consent) {
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
        if(consent.getConsentStage().equals(ConsentStage.SAVED)) {
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
            //generate pdf
            PatientDto patientDto = phrService.getPatientProfile();
            consent.setSavedPdf(consentPdfGenerator.generate42CfrPart2Pdf(consent, patientDto, false, new Date(), consentAttestationTermRepository.findOne(Long.valueOf(1)).getText()));

            consentRepository.save(consent);
        }
    }


    @Override
    public void revokeConsent(Long patientId, Long consentId, ConsentRevocationDto consentRevocationDto) {

        //get patient
        final Patient patient = patientRepository.saveAndGet(patientId);

        Consent consent = consentRepository.findOne(consentId);

        ConsentRevocationTerm consentRevocationTerm = consentRevocationTermRepository.findOne(Long.valueOf(1));

        if (consentRevocationDto.isAcceptTerms() && consent.getConsentStage().equals(ConsentStage.SIGNED)) {

            //build consentRevocation
            final ConsentRevocation consentRevocation = ConsentRevocation.builder()
                    .consentRevocationTerm(consentRevocationTerm)
                    .consent(consent)
                    .build();

            //update consent
            consent.setConsentStage(ConsentStage.REVOKED);


            PatientDto patientDto = phrService.getPatientProfile();
            consentRevocation.setConsentRevocationPdf(consentRevocationPdfGenerator.generateConsentRevocationPdf(consent, patientDto, new Date(), consentRevocationTerm.getText()));

            consent.setConsentRevocation(consentRevocation);

            consentRepository.save(consent);
        } else throw new BadRequestException();
    }

    @Override
    public Object getConsent(Long patientId, Long consentId, String format) {
        Consent consent = consentRepository.findOne(consentId);
        if (format != null && format.equals("pdf") && consent.getConsentStage().equals(ConsentStage.SAVED)) {
            return new ContentDto("application/pdf", consent.getSavedPdf());
        } else
            return toConsentDto(consent);
    }

    @Override
    public Object getAttestedConsent(Long patientId, Long consentId, String format) {
        Consent consent = consentRepository.findOne(consentId);
        if (format != null && format.equals("pdf") && (!consent.getConsentStage().equals(ConsentStage.SAVED))) {
            return new ContentDto("application/pdf", consent.getConsentAttestation().getConsentAttestationPdf());
        } else
            return mapToDetailedConsentDto(consent);

    }


    @Override
    public Object getRevokedConsent(Long patientId, Long consentId, String format) {
        Consent consent = consentRepository.findOne(consentId);
        if (format != null && format.equals("pdf") && consent.getConsentStage().equals(ConsentStage.REVOKED)) {
            return new ContentDto("application/pdf", consent.getConsentRevocation().getConsentRevocationPdf());
        } else
            return mapToDetailedConsentDto(consent);
    }

    private ConsentDto toConsentDto(Consent consent){
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
                .build();

    }

}
