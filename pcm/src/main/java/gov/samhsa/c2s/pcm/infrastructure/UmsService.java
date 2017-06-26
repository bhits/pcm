package gov.samhsa.c2s.pcm.infrastructure;

import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.TelecomDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;

@FeignClient(name = "ums")
@Service
public interface UmsService {

    @RequestMapping(value = "/patients/{mrn}", method = RequestMethod.GET)
    PatientDto getPatientProfile(@PathVariable("mrn") String mrn);

    /**
     * TODO:// Uncomment this getUserById API call when users are created in UMS for Provider/Staff users
     * @param userAuthId
     * @return
     *
     * @RequestMapping(value = "/authId/{userAuthId}", method = RequestMethod.GET)
        UserDto getUserById(@PathVariable("userAuthId") String userAuthId);
     */

    default UserDto getUserById(String userAuthId) {
        TelecomDto defaultEmail = new TelecomDto();
        defaultEmail.setSystem("EMAIL");
        defaultEmail.setValue("c2s-provider@mailinator.com");
        return UserDto.builder()
                .firstName("Bob")
                .lastName("Provider")
                .telecoms(Arrays.asList(defaultEmail))
                .build();
    }

}
