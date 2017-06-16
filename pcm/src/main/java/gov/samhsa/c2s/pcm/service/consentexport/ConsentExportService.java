package gov.samhsa.c2s.pcm.service.consentexport;


import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface ConsentExportService {

    /**
     * Export consent to xacml format.
     *
     * @param xacmlRequestDto the consent
     * @return the string
     */
    Object exportConsent2XACML(XacmlRequestDto xacmlRequestDto) throws ConsentGenException;
}

