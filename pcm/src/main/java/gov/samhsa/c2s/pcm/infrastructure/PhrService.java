package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name = "phr")
public interface PhrService {

    // FIXME: call actual PHR service instead of returning mock data after PHR is adjusted as a core service
    @RequestMapping(value = "/patients", method = RequestMethod.GET)
    default PatientDto getPatientProfile() {
        return PatientDto.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Recruit")
                .genderCode("female")
                .email("alice.recruit@mailinator.com")
                .address("1111 Main Street")
                .city("Columbia")
                .stateCode("MD")
                .zip("22222")
                .birthDate(new Date())
                .build();
    }
}
