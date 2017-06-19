package gov.samhsa.c2s.pcm.service.consentexport;


import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;

public interface ConsentExportService {

    /**
     * Export consent to xacml format.
     *
     * @param xacmlRequestDto the consent
     * @return the string
     */
    Object exportConsent2XACML(XacmlRequestDto xacmlRequestDto);
}

