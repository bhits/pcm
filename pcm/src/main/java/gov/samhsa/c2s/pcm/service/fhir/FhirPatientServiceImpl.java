package gov.samhsa.c2s.pcm.service.fhir;


import gov.samhsa.c2s.pcm.config.FhirProperties;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientIdentifierDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.TelecomDto;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
public class FhirPatientServiceImpl implements FhirPatientService {

    @Autowired
    private FhirProperties fhirProperties;



    @Override
    public Patient getFhirPatient(PatientDto patientDto) {
        return patientDtoToPatient.apply(patientDto);
    }

    Function<PatientDto, Patient> patientDtoToPatient = new Function<PatientDto, Patient>() {
        @Override
        public Patient apply(PatientDto patientDto) {
            // set patient information
            Patient fhirPatient = new Patient();

            //setting mandatory fields

            fhirPatient.addName().setFamily(patientDto.getLastName()).addGiven(patientDto.getFirstName());
            Optional<TelecomDto> email= patientDto.getTelecoms().stream().filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(ContactPoint.ContactPointSystem.EMAIL.toString())).findFirst();
            if(email.isPresent())
            fhirPatient.addTelecom().setValue(email.get().getValue()).setSystem(ContactPoint.ContactPointSystem.EMAIL);
            fhirPatient.setBirthDate(Date.valueOf(patientDto.getBirthDate()));
            fhirPatient.setGender(getPatientGender.apply(patientDto.getGenderCode()));
            fhirPatient.setActive(true);

            //Add an Identifier
            setIdentifiers(fhirPatient, patientDto);

            //optional fields
            fhirPatient.addAddress().addLine(patientDto.getAddresses().get(0).getLine1()).setCity(patientDto.getAddresses().get(0).getCity()).setState(patientDto.getAddresses().get(0).getStateCode()).setPostalCode(patientDto.getAddresses().get(0).getPostalCode());
            return fhirPatient;
        }
    };


    private void setIdentifiers(Patient patient, PatientDto patientDto) {


        for(PatientIdentifierDto pidDto : patientDto.getPatientIdentifiers()) {
            if(pidDto.getSystem().equalsIgnoreCase(fhirProperties.getMrn().getSystem())) {
                //setting patient mrn
                patient.addIdentifier().setSystem(fhirProperties.getMrn().getSystem())
                        .setUse(Identifier.IdentifierUse.OFFICIAL).setValue(pidDto.getValue());
                patient.setId(new IdType(pidDto.getValue()));
            } else if(pidDto.getSystem().equalsIgnoreCase(fhirProperties.getSsn().getSystem())) {
                String ssnValue =  pidDto.getValue();
                // setting ssn value
                if (null != ssnValue && !ssnValue.isEmpty())
                    patient.addIdentifier().setSystem(fhirProperties.getSsn().getSystem())
                            .setValue(ssnValue);
            }
        }
        Optional<TelecomDto> telephone=patientDto.getTelecoms().stream().filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(ContactPoint.ContactPointSystem.PHONE.toString())).findFirst();
        if (telephone.isPresent())

            patient.addTelecom().setValue(telephone.get().getValue()).setSystem(ContactPoint.ContactPointSystem.PHONE);
    }

    Function<String, AdministrativeGender> getPatientGender = new Function<String, AdministrativeGender>() {
        @Override
        public AdministrativeGender apply(String codeString) {
            if (codeString != null && !"".equals(codeString) || codeString != null && !"".equals(codeString)) {
                if ("male".equalsIgnoreCase(codeString) || "M".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.MALE;
                } else if ("female".equalsIgnoreCase(codeString) || "F".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.FEMALE;
                } else if ("other".equalsIgnoreCase(codeString) || "O".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.OTHER;
                } else if ("unknown".equalsIgnoreCase(codeString) || "UN".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.UNKNOWN;
                } else {
                    throw new IllegalArgumentException("Unknown AdministrativeGender code \'" + codeString + "\'");
                }
            } else {
                return null;
            }
        }
    };


}
