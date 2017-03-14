package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/patients/{patientId}")
public class ConsentRestController {

    @GetMapping("/consents")
    public List<ConsentDto> getConsents(@PathVariable Long patientId) {
        log.info("Patient ID: {}", patientId);
        throw new NotImplementedException();
    }
}
