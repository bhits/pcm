package gov.samhsa.c2s.pcm.service.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import gov.samhsa.c2s.pcm.config.FhirProperties;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.infrastructure.VssService;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.ValueSetCategoryDto;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.codesystems.V3ActCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FhirConsentServiceImpl implements FhirConsentService {


    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private FhirValidator fhirValidator;

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirPatientService fhirPatientService;

    @Autowired
    private UniqueOidProvider uniqueOidProvider;

    @Autowired
    private FhirProperties fhirProperties;
    @Autowired
    private VssService vssService;


    @Override
    public byte[] getAttestedFhirConsent(gov.samhsa.c2s.pcm.domain.Consent c2sConsent, PatientDto patientDto) {
        /*
        Use the client to store a new consent resource instance
        Invoke the server create method (and send pretty-printed JSON
        encoding to the server
        instead of the default which is non-pretty printed XML)
        invoke Consent service
        */

        Consent fhirConsent = createFhirConsent(c2sConsent, patientDto);
        //validate the resource
        ValidationResult validationResult = fhirValidator.validateWithResult(fhirConsent);

        log.debug("validationResult.isSuccessful(): " + validationResult.isSuccessful());
        //throw format error if the validation is not successful
        if (!validationResult.isSuccessful()) {
            throw new FHIRFormatErrorException("Consent Validation is not successful" + validationResult.getMessages());
        }
        //publish fhir consent to fhir server
        fhirClient.create().resource(fhirConsent).execute();

        return fhirContext.newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(fhirConsent).getBytes();

    }

    @Override
    public byte[] getRevokedFhirConsent(gov.samhsa.c2s.pcm.domain.Consent c2sConsent, PatientDto patientDto) {

        // consent by identifier on FHIR server
        Consent fhirConsent = createFhirConsent(c2sConsent, patientDto);
        fhirConsent.setStatus(Consent.ConsentStatus.INACTIVE);

        //validate the resource
        ValidationResult validationResult = fhirValidator.validateWithResult(fhirConsent);

        log.debug("validationResult.isSuccessful(): " + validationResult.isSuccessful());
        //throw format error if the validation is not successful
        if (!validationResult.isSuccessful()) {
            throw new FHIRFormatErrorException("Consent Validation is not successful" + validationResult.getMessages());
        }

        //revoke fhir consent to fhir server
        fhirClient.update().resource(fhirConsent)
                .conditional()
                .where(Consent.IDENTIFIER.exactly().systemAndCode(fhirProperties.getMrn().getSystem(), c2sConsent.getConsentReferenceId()))
                .execute();

        return fhirContext.newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(fhirConsent).getBytes();

    }

    public Consent createFhirConsent(gov.samhsa.c2s.pcm.domain.Consent consent, PatientDto patientDto) {
        return createGranularConsent(consent, patientDto);
    }

    private Consent createBasicConsent(gov.samhsa.c2s.pcm.domain.Consent c2sConsent, PatientDto patientDto) {


        Consent fhirConsent = new Consent();

        // set the id as a concatenated "OID.consentId"
        fhirConsent.setId(new IdType(c2sConsent.getId()));

        // Set patient reference and add patient as contained resource
        if (fhirProperties.isPatientReference()) {
            String patientResourceId = fhirPatientService.getPatientResourceId(fhirProperties.getMrn().getSystem(), patientDto.getMrn());
            fhirConsent.getPatient().setReference("Patient/" + patientResourceId);

            // Consent signature details
            Reference consentSignature = new Reference();
            consentSignature.setDisplay(patientDto.getFirstName() + " " + patientDto.getLastName());
            consentSignature.setReference("Patient/" + patientResourceId);
            fhirConsent.getConsentor().add(consentSignature);

        } else {
            Patient fhirPatient = fhirPatientService.getFhirPatient(patientDto);
            fhirConsent.getPatient().setReference("#" + fhirPatient.getId());
            fhirConsent.getContained().add(fhirPatient);

            // Consent signature details
            Reference consentSignature = new Reference();
            consentSignature.setDisplay(fhirPatient.getNameFirstRep().getNameAsSingleString());
            consentSignature.setReference("#" + patientDto.getId());
            fhirConsent.getConsentor().add(consentSignature);

        }


        // consent status
        fhirConsent.setStatus(Consent.ConsentStatus.ACTIVE);


        // Specify Authors, the providers authorizes to disclose
        // Author :: Organizational Provider
        // Organization sourceOrganizatiOrganizationalProviderPermittedToDiscloseonResource = null;
        Organization sourceOrganizationResource = null;
        if (c2sConsent.getConsentAttestation().getFromOrganizations().size() > 0) {
            sourceOrganizationResource = setOrganizationProvider(
                    c2sConsent.getConsentAttestation().getFromOrganizations().get(0));
        }

        if (null != sourceOrganizationResource) {
            fhirConsent.getContained().add(sourceOrganizationResource);
            fhirConsent.getOrganization().setReference("#" + sourceOrganizationResource.getId());
            // TODO :: Need to add source organization details to patient object
        } else {
            // Author :: Individual Provider
            Practitioner sourcePractitioner = null;
            if (c2sConsent.getConsentAttestation().getFromPractitioners().size() > 0) {
                sourcePractitioner = setPractitionerProvider(
                        c2sConsent.getConsentAttestation().getFromPractitioners().get(0));
                fhirConsent.getContained().add(sourcePractitioner);
                fhirConsent.getOrganization().setReference("#" + sourcePractitioner.getId());
                // TODO :: Need to add source organization details to patient object
            }

        }

        // Specify Policy - Reference the "default" OAuth2 policy that covers the related information
        fhirConsent.setPolicy(c2sConsent.getConsentReferenceId());

        // Specify Recipients, the providers disclosure is made to Recipient :: Organizational Provider
        Organization recipientOrganization = null;
        if (c2sConsent.getConsentAttestation().getToOrganizations().size() > 0) {
            recipientOrganization = setOrganizationProvider(
                    c2sConsent.getConsentAttestation().getToOrganizations().get(0));
        }
        if (null != recipientOrganization) {
            fhirConsent.getContained().add(recipientOrganization);
            fhirConsent.getRecipient().add(new Reference().setReference("#" + recipientOrganization.getId()));
        } else {
            // Recipient :: Individual Provider
            Practitioner recipientPractitioner = null;
            if (c2sConsent.getConsentAttestation().getToPractitioners().size() > 0) {
                recipientPractitioner = setPractitionerProvider(
                        c2sConsent.getConsentAttestation().getToPractitioners().get(0));
                fhirConsent.getContained().add(recipientPractitioner);
                fhirConsent.getRecipient().add(new Reference().setReference("#" + recipientPractitioner.getId()));
            }
        }


        // set POU
        for (Purpose purpose : c2sConsent.getConsentAttestation().getConsent().getPurposes()) {
            String pou = purpose.getIdentifier().getValue();
            Coding coding = new Coding(fhirProperties.getPou().getSystem(), pou, purpose.getDisplay());
            fhirConsent.getPurpose().add(coding);
        }

        // set terms of consent and intended recipient(s)
        fhirConsent.getPeriod().setStart(
                Date.from(c2sConsent.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
        fhirConsent.getPeriod().setEnd(
                Date.from(c2sConsent.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
        );

        // consent sign time
        fhirConsent.setDateTime(new Date());

        // set identifier for this consent
        fhirConsent.getIdentifier().setSystem(fhirProperties.getMrn().getSystem()).setValue(c2sConsent.getConsentReferenceId());

        //set category
        CodeableConcept categoryConcept = new CodeableConcept();

        categoryConcept.addCoding(
                new Coding().setCode(V3ActCode.IDSCL.toCode())
                        .setSystem(V3ActCode.IDSCL.getSystem())
                        .setDisplay(V3ActCode.IDSCL.getDisplay())
        );
        fhirConsent.getCategory().add(categoryConcept);

        return fhirConsent;
    }

    private Organization setOrganizationProvider(gov.samhsa.c2s.pcm.domain.Organization organization) {
        Organization sourceOrganizationResource = new Organization();
        String orgNpi = organization.getProvider().getIdentifier().getValue();
        sourceOrganizationResource.setId(new IdType(orgNpi));
        sourceOrganizationResource.addIdentifier().setSystem(fhirProperties.getNpi().getSystem()).setValue(orgNpi);
        sourceOrganizationResource.setName(organization.getName());
        sourceOrganizationResource.addAddress().addLine(organization.getAddress().getLine1())
                .setCity(organization.getAddress().getCity())
                .setState(organization.getAddress().getState())
                .setPostalCode(organization.getAddress().getPostalCode());
        return sourceOrganizationResource;
    }

    private Practitioner setPractitionerProvider(gov.samhsa.c2s.pcm.domain.Practitioner practitioner) {
        Practitioner sourcePractitionerResource = new Practitioner();
        String practionerNPI = practitioner.getProvider().getIdentifier().getValue();
        sourcePractitionerResource.setId(new IdType(practionerNPI));
        sourcePractitionerResource.addIdentifier().setSystem(fhirProperties.getNpi().getSystem()).setValue(practionerNPI);
        //setting the name element
        HumanName indName = new HumanName();
        indName.setFamily(practitioner.getLastName());
        indName.addGiven(practitioner.getFirstName());
        sourcePractitionerResource.addName(indName);
        //setting the address
        sourcePractitionerResource.addAddress().addLine(practitioner.getAddress().getLine1())
                .setCity(practitioner.getAddress().getCity())
                .setState(practitioner.getAddress().getState())
                .setPostalCode(practitioner.getAddress().getPostalCode());

        return sourcePractitionerResource;
    }


    private Consent createGranularConsent(gov.samhsa.c2s.pcm.domain.Consent c2sConsent, PatientDto patientDto) {
        // get basic consent details
        Consent fhirConsent = createBasicConsent(c2sConsent, patientDto);


        // get share categories from consent
        List<String> shareCodes = c2sConsent.getSensitivityCategories()
                .stream()
                .map(codes -> codes.getIdentifier().getValue())
                .collect(Collectors.toList());


        List<Coding> includeCodingList = new ArrayList<>();
        //Get all sensitive categories from vss
        List<ValueSetCategoryDto> allSensitiveCategories = vssService.getValueSetCategories();


        // go over full list and add obligation as exclusions
        for (ValueSetCategoryDto valueSetCategoryDto : allSensitiveCategories) {
            if (shareCodes.contains(valueSetCategoryDto.getCode())) {
                String systemUrl = valueSetCategoryDto.getSystem();
                String code = valueSetCategoryDto.getCode();
                if (!(code.equalsIgnoreCase(V3ActCode.ETH.toCode())
                        || code.equalsIgnoreCase(V3ActCode.PSY.toCode())
                        || code.equalsIgnoreCase(V3ActCode.SEX.toCode())
                        || code.equalsIgnoreCase(V3ActCode.HIV.toCode())
                )) {
                    systemUrl = fhirProperties.getMrn().getSystem();
                }
                // include it
                includeCodingList.add(
                        new Coding(systemUrl
                                , code
                                , valueSetCategoryDto.getName()));
            }
        }

        // add list to consent
        Consent.ExceptComponent exceptComponent = new Consent.ExceptComponent();

        //List of included Sensitive policy codes
        exceptComponent.setSecurityLabel(includeCodingList);
        exceptComponent.setType(Consent.ConsentExceptType.PERMIT);

        fhirConsent.setExcept(Collections.singletonList(exceptComponent));

        //logs FHIRConsent into json and xml format in debug mode
        logFHIRConsent(fhirConsent);


        return fhirConsent;

    }

    private void logFHIRConsent(Consent fhirConsent) {
        log.debug(fhirContext.newXmlParser().setPrettyPrint(true)
                .encodeResourceToString(fhirConsent));
        log.debug(fhirContext.newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(fhirConsent));
    }


}
