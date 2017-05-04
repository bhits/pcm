package gov.samhsa.c2s.pcm.service.fhir;


import gov.samhsa.c2s.pcm.config.FhirProperties;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
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
            fhirPatient.setBirthDate(Date.valueOf(patientDto.getBirthDate()));
            fhirPatient.setGender(getPatientGender.apply(patientDto.getGenderCode()));
            fhirPatient.setActive(true);

            //Add an Identifier
            setIdentifiers(fhirPatient, patientDto);

            //optional fields
            patientDto.getAddresses().stream().forEach(addressDto ->
                    fhirPatient.addAddress().addLine(addressDto.getLine1()).addLine(addressDto.getLine2()).setCity(addressDto.getCity()).setState(addressDto.getStateCode()).setPostalCode(addressDto.getPostalCode())
            );

            patientDto.getTelecoms().stream().forEach(telecomDto ->
                    fhirPatient.addTelecom().setSystem(ContactPoint.ContactPointSystem.valueOf(telecomDto.getSystem())).setUse(ContactPoint.ContactPointUse.valueOf(telecomDto.getUse())).setValue(telecomDto.getValue())
            );
            return fhirPatient;
        }
    };


    private void setIdentifiers(Patient patient, PatientDto patientDto) {


        //setting patient mrn
        patient.addIdentifier().setSystem(fhirProperties.getMrn().getSystem())
                .setUse(Identifier.IdentifierUse.OFFICIAL).setValue(patientDto.getMrn());

        patient.setId(new IdType(patientDto.getMrn()));

        // setting ssn value
        String ssnValue = patientDto.getSocialSecurityNumber();
        if (null != ssnValue && !ssnValue.isEmpty())
            patient.addIdentifier().setSystem(fhirProperties.getSsn().getSystem())
                    .setValue(ssnValue);
    }

    Function<String, AdministrativeGender> getPatientGender = new Function<String, AdministrativeGender>() {
        @Override
        public Enumerations.AdministrativeGender apply(String codeString) {
            switch (codeString.toUpperCase()) {
                case "MALE":
                    return Enumerations.AdministrativeGender.MALE;
                case "M":
                    return Enumerations.AdministrativeGender.MALE;
                case "FEMALE":
                    return Enumerations.AdministrativeGender.FEMALE;
                case "F":
                    return Enumerations.AdministrativeGender.FEMALE;
                case "OTHER":
                    return Enumerations.AdministrativeGender.OTHER;
                case "O":
                    return Enumerations.AdministrativeGender.OTHER;
                case "UNKNOWN":
                    return Enumerations.AdministrativeGender.UNKNOWN;
                case "UN":
                    return Enumerations.AdministrativeGender.UNKNOWN;
                default:
                    return Enumerations.AdministrativeGender.UNKNOWN;

            }
        }
    };


}
