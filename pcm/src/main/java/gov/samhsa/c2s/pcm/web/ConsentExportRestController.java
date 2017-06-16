package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.pcm.service.consentexport.ConsentExportService;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/patients/{patientId}")
public class ConsentExportRestController {

    private final ConsentExportService consentExportService;

    public ConsentExportRestController(ConsentExportService consentExportService) {
        this.consentExportService = consentExportService;
    }


    @PostMapping("/consents/search/xacml")
    public Object getXACMLConsent(@Valid @RequestBody XacmlRequestDto xacmlRequestDto) throws ConsentGenException {
        return consentExportService.exportConsent2XACML(xacmlRequestDto);
    }
}
