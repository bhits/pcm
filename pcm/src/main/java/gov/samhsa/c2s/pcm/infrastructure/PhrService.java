package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientIdentifierDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@FeignClient(name = "phr")
@Service
public interface PhrService {


    // FIXME: call actual PHR service instead of returning mock data after PHR is adjusted as a core service
    @RequestMapping(value = "/patients", method = RequestMethod.GET)
    default PatientDto getPatientProfile() {
        return PatientDto.builder()
                .id("4")
                .firstName("Alice")
                .lastName("Recruit")
                .genderCode("female")
                .email("alice.recruit@mailinator.com")
                .address("1111 Main Street")
                .city("Columbia")
                .stateCode("MD")
                .zip("22222")
                .birthDate(new Date())
                .patientIdentifiers(gePatientIdentifierDtos())
                .build();
    }

    default List<PatientIdentifierDto> gePatientIdentifierDtos(){

        PatientIdentifierDto mrnPatientId = PatientIdentifierDto.builder()
                .system("https://bhits.github.io/consent2share/")
                .value("PID-4")
                .label("MRN")
                .build();
        PatientIdentifierDto ssnPatientId = PatientIdentifierDto.builder()
                .system("http://hl7.org/fhir/sid/us-ssn")
                .value("123456789")
                .label("SSN")
                .build();

        return Arrays.asList(mrnPatientId, ssnPatientId);

    }
}
