package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientIdentifierDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.List;

@FeignClient(name = "ums")
@Service
public interface UmsService {

    @RequestMapping(value = "/patients/{mrn}", method = RequestMethod.GET)
    PatientDto getPatientProfile(@PathVariable("mrn") String mrn);

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

    @RequestMapping(value = "/authId/{userAuthId}", method = RequestMethod.GET)
    UserDto getUserById(@PathVariable("userAuthId") String userAuthId);


}
