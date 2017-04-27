package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.ProviderService;
import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifiersDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@RestController
@Slf4j
@RequestMapping("/patients/{patientId}")
public class ProviderRestController {

    @Autowired
    private ProviderService providerService;

    @GetMapping("/providers")
    public List<AbstractProviderDto> getProviders(@PathVariable String patientId) {
        return providerService.getProviders(patientId);
    }

    @PostMapping("/providers")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveProviders(@PathVariable String patientId,
                              @Valid @RequestBody IdentifiersDto providerIdentifiersDto) {
        providerService.saveProviders(patientId, providerIdentifiersDto.getIdentifiers());
    }

    @DeleteMapping("/providers/{providerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProvider(@PathVariable String patientId,
                               @PathVariable Long providerId) {
        providerService.deleteProvider(patientId, providerId);
    }
}
