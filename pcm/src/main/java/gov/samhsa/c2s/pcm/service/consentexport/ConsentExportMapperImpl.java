package gov.samhsa.c2s.pcm.service.consentexport;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.common.consentgen.IndividualProviderDto;
import gov.samhsa.c2s.common.consentgen.OrganizationalProviderDto;
import gov.samhsa.c2s.common.consentgen.PatientDto;
import gov.samhsa.c2s.common.consentgen.TypeCodesDto;
import gov.samhsa.c2s.pcm.config.FhirProperties;
import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import gov.samhsa.c2s.pcm.service.dto.SensitivityCategoryDto;
import gov.samhsa.c2s.pcm.service.exception.ConsentExportException;
import gov.samhsa.c2s.pcm.service.exception.InvalidProviderTypeException;
import gov.samhsa.c2s.pcm.service.exception.NpiNotMappedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ConsentExportMapperImpl implements ConsentExportMapper {

    private final FhirProperties fhirProperties;

    public ConsentExportMapperImpl(FhirProperties fhirProperties) {
        this.fhirProperties = fhirProperties;
    }

    @Override
    public ConsentDto map(DetailedConsentDto
                                  pcmDetailedConsentDto, gov.samhsa.c2s.pcm
                                  .infrastructure.dto.PatientDto
                                  pcmPatientDto) {
        log.debug("Invoking ConsentExportMapperImpl - map - Start");
        ConsentDto consentDto = new ConsentDto();
        PatientDto patientDto = new PatientDto();

        patientDto.setMedicalRecordNumber(pcmPatientDto.getMrn());
        patientDto.setLastName(pcmPatientDto.getLastName());
        patientDto.setFirstName(pcmPatientDto.getFirstName());

        /* Map PCM consent fields to ConsentGen ConsentDto fields */

        // Map patient Dto
        consentDto.setPatientDto(patientDto);

        // Map consent reference ID
        consentDto.setConsentReferenceid(pcmDetailedConsentDto.getConsentReferenceId());

        // Map consent start, end, and signed dates
        consentDto.setConsentStart(Date.from(pcmDetailedConsentDto.getStartDate().atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
        consentDto.setConsentEnd(Date.from(pcmDetailedConsentDto.getEndDate().atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
        consentDto.setSignedDate(Date.from(pcmDetailedConsentDto.getEndDate().atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

        try {
            // Map providers permitted to disclose (i.e. "from" providers)
            mapProvidersPermittedToDisclose(consentDto, pcmDetailedConsentDto);

            // Map providers to which disclosure is made (i.e. "to" providers)
            mapProvidersDisclosureIsMadeTo(consentDto, pcmDetailedConsentDto);
            // Map share for purpose of use codes
            mapShareForPurposeOfUseCodes(consentDto, pcmDetailedConsentDto);

            // Map share sensitivity policy codes
            mapShareSensitivityPolicyCodes(consentDto, pcmDetailedConsentDto);

        } catch (ConsentGenException e) {
            log.error("Error while mapping PCM Consent details to Consent Gen Consent Dto ");
            throw new ConsentExportException(e.getMessage(), e);
        }
        log.debug("Invoking ConsentExportMapperImpl - map - End" + consentDto);
        return consentDto;

    }


    private void mapProvidersPermittedToDisclose(ConsentDto consentDto, DetailedConsentDto
            pcmDetailedConsentDto) throws ConsentGenException {
        //DomainResource fhirFromProviderResource = (DomainResource) fhirConsent.getOrganization().getResource();
        consentDto.setProvidersPermittedToDisclose(new HashSet<>());
        consentDto.setOrganizationalProvidersPermittedToDisclose(new HashSet<>());

        List<AbstractProviderDto> fromProviders = pcmDetailedConsentDto.getFromProviders();
        Set<OrganizationalProviderDto> organizationalProviderDtoSet = new HashSet<>();
        Set<IndividualProviderDto> individualProviderDtoSet = new HashSet<>();

        for (AbstractProviderDto fromProvider : fromProviders) {
            if (AbstractProviderDto.ProviderType.ORGANIZATION.equals(fromProvider.getProviderType())) {

                OrganizationalProviderDto organizationalProviderDto = new OrganizationalProviderDto();
                organizationalProviderDto.setNpi(
                        fromProvider.getIdentifiers().stream()
                                .filter(identfierDto -> fhirProperties.getNpi().getSystem().equalsIgnoreCase(
                                        identfierDto
                                                .getSystem()))
                                .map(i -> i.getValue()).findAny().orElseThrow(NpiNotMappedException::new));

                organizationalProviderDtoSet.add(organizationalProviderDto);
            } else if (AbstractProviderDto.ProviderType.PRACTITIONER.equals(fromProvider.getProviderType())) {
                IndividualProviderDto individualProviderDto = new IndividualProviderDto();
                individualProviderDto.setNpi(
                        fromProvider.getIdentifiers().stream()
                                .filter(identfierDto -> fhirProperties.getNpi().getSystem().equalsIgnoreCase(
                                        identfierDto
                                                .getSystem()))
                                .map(i -> i.getValue()).findAny().orElseThrow(NpiNotMappedException::new));

                individualProviderDtoSet.add(individualProviderDto);

            } else {
                throw new InvalidProviderTypeException("Invalid from provider resource type found in PCM consent; " +
                        "ResourceType of PCM Consent must be either 'Organization' or 'Practitioner'");
            }
        }
        consentDto.setOrganizationalProvidersPermittedToDisclose(organizationalProviderDtoSet);
        consentDto.setProvidersPermittedToDisclose(individualProviderDtoSet);
    }

    private void mapProvidersDisclosureIsMadeTo(ConsentDto consentDto, DetailedConsentDto
            pcmDetailedConsentDto) throws ConsentGenException {
        //DomainResource fhirFromProviderResource = (DomainResource) fhirConsent.getOrganization().getResource();
        consentDto.setProvidersDisclosureIsMadeTo(new HashSet<>());
        consentDto.setOrganizationalProvidersDisclosureIsMadeTo(new HashSet<>());

        List<AbstractProviderDto> toProviders = pcmDetailedConsentDto.getToProviders();
        Set<OrganizationalProviderDto> organizationalProviderDtoSet = new HashSet<>();
        Set<IndividualProviderDto> individualProviderDtoSet = new HashSet<>();

        for (AbstractProviderDto toProvider : toProviders) {
            if (AbstractProviderDto.ProviderType.ORGANIZATION.equals(toProvider.getProviderType())) {

                OrganizationalProviderDto organizationalProviderDto = new OrganizationalProviderDto();
                organizationalProviderDto.setNpi(
                        toProvider.getIdentifiers().stream()
                                .filter(identfierDto -> fhirProperties.getNpi().getSystem().equalsIgnoreCase(
                                        identfierDto
                                                .getSystem()))
                                .map(i -> i.getValue()).findAny().orElseThrow(NpiNotMappedException::new));

                organizationalProviderDtoSet.add(organizationalProviderDto);
            } else if (AbstractProviderDto.ProviderType.PRACTITIONER.equals(toProvider.getProviderType())) {
                IndividualProviderDto individualProviderDto = new IndividualProviderDto();
                individualProviderDto.setNpi(
                        toProvider.getIdentifiers().stream()
                                .filter(identfierDto -> fhirProperties.getNpi().getSystem().equalsIgnoreCase(
                                        identfierDto
                                                .getSystem()))
                                .map(i -> i.getValue()).findAny().orElseThrow(NpiNotMappedException::new));

                individualProviderDtoSet.add(individualProviderDto);

            } else {
                throw new InvalidProviderTypeException("Invalid from provider resource type found in PCM consent; " +
                        "ResourceType of PCM Consent must be either 'Organization' or 'Practitioner'");
            }
        }
        consentDto.setOrganizationalProvidersDisclosureIsMadeTo(organizationalProviderDtoSet);
        consentDto.setProvidersDisclosureIsMadeTo(individualProviderDtoSet);
    }

    /**
     * Maps the share for purpose of use codes from the FHIR Consent object to the ConsentDto object.
     *
     * @param consentDto    - The ConsentDto object into which the share for purpose of use codes should be mapped
     * @param pcmConsentDto - The PCM Consent object which contains the share for purpose of use codes to be mapped
     *                      into consentDto
     * @return The ConsentDto object which contains the mapped share for purpose of use codes
     * @throws ConsentGenException - Thrown when FHIR consent contains no 'purpose' codes, or when extracted purpose
     *                             of use codes set is empty
     */
    private void mapShareForPurposeOfUseCodes(ConsentDto consentDto, DetailedConsentDto pcmConsentDto) throws
            ConsentGenException {

        List<PurposeDto> pcmPurposeDtos = pcmConsentDto.getPurposes();

        Set<TypeCodesDto> consentDtoShareForPurposeOfUseCodes = new HashSet<>();

        if (pcmPurposeDtos.size() > 0) {
            pcmPurposeDtos.forEach(pou -> {
                log.debug("pcm pou dto" + pcmPurposeDtos);
                TypeCodesDto pouCodeDto = new TypeCodesDto();

                pouCodeDto.setCodeSystem(pou.getIdentifier().getSystem());
                pouCodeDto.setCode(pou.getIdentifier().getValue());

                if (!pou.getDisplay().isEmpty()) {
                    pouCodeDto.setDisplayName(pou.getDisplay());
                }
                consentDtoShareForPurposeOfUseCodes.add(pouCodeDto);
            });
        } else {
            throw new ConsentGenException("Share for purpose of use codes set extracted from PCM consent is an empty " +
                    "set");
        }

        consentDto.setShareForPurposeOfUseCodes(consentDtoShareForPurposeOfUseCodes);
        log.debug("consentgen pou code" + consentDtoShareForPurposeOfUseCodes);
    }

    /**
     * Maps the share sensitivity policy codes from the FHIR Consent object to the ConsentDto object.
     *
     * @param consentDto            - The ConsentDto object into which the sensitivity policy codes should be mapped
     * @param pcmDetailedConsentDto - The FHIR Consent object which contains the sensitivity policy codes to be
     *                              mapped into consentDto
     * @return The ConsentDto object which contains the mapped sensitivity policy codes
     * @throws ConsentGenException - Thrown when FHIR consent contains no 'except' anr/or 'securityLabel' codes, when
     *                             the codes in the FHIR
     *                             consent are of a type other than 'permit', or when the extracted sensitivity
     *                             policy codes set size is != 1
     */
    private void mapShareSensitivityPolicyCodes(ConsentDto consentDto, DetailedConsentDto pcmDetailedConsentDto)
            throws ConsentGenException {

        List<SensitivityCategoryDto> sensitivityCategoryDtos = pcmDetailedConsentDto.getSensitivityCategories();

        Set<TypeCodesDto> consentDtoShareSensitivityPolicyCodes = new HashSet<>();


        if (sensitivityCategoryDtos.size() > 0) {
            sensitivityCategoryDtos.forEach(sensitivityPolicyCode -> {
                TypeCodesDto sensitivityPolicyCodeDto = new TypeCodesDto();

                sensitivityPolicyCodeDto.setCodeSystem(sensitivityPolicyCode.getIdentifier().getSystem());
                sensitivityPolicyCodeDto.setCode(sensitivityPolicyCode.getIdentifier().getValue());

                if (!sensitivityPolicyCode.getDisplay().isEmpty()) {
                    sensitivityPolicyCodeDto.setDisplayName(sensitivityPolicyCode.getDisplay());
                }

                consentDtoShareSensitivityPolicyCodes.add(sensitivityPolicyCodeDto);
            });
        } else {
            throw new ConsentGenException("Sensitivity policy codes set extracted from PCM consent is an empty set");
        }

        consentDto.setShareSensitivityPolicyCodes(consentDtoShareSensitivityPolicyCodes);
    }
}
