package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ums")
public interface UmsService {

    @RequestMapping(value = "/patients/{mrn}", method = RequestMethod.GET)
    PatientDto getPatientProfile(@PathVariable("mrn") String mrn);

    @RequestMapping(value = "/patients", method = RequestMethod.GET)
    PatientDto getPatientByIdentifierValueAndIdentifierSystem(@RequestParam("identifierValue") String identifierValue, @RequestParam("identifierSystem") String identifierSystem);

    @RequestMapping(value = "/users/authId/{userAuthId}", method = RequestMethod.GET)
    UserDto getUserById(@PathVariable("userAuthId") String userAuthId);
}
