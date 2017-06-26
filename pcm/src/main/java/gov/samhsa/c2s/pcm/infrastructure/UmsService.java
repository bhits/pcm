package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "ums")
@Service
public interface UmsService {

    @RequestMapping(value = "/patients/{mrn}", method = RequestMethod.GET)
    PatientDto getPatientProfile(@PathVariable("mrn") String mrn);

    @RequestMapping(value = "/authId/{userAuthId}", method = RequestMethod.GET)
    UserDto getUserById(@PathVariable("userAuthId") String userAuthId);


}
