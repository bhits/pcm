package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.pcm.service.consentexport.ConsentExportService;
import gov.samhsa.c2s.pcm.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ConsentExportRestController {

    private final ConsentExportService consentExportService;

    public ConsentExportRestController(ConsentExportService consentExportService) {
        this.consentExportService = consentExportService;
    }


    @RequestMapping(value = "/consents/export/xacml", method = RequestMethod.POST)
    public ConsentXacmlDto exportXACMLConsent(@Valid @RequestBody XacmlRequestDto xacmlRequestDto)  {
        return consentExportService.exportConsent2XACML(xacmlRequestDto);
    }
}
