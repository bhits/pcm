package gov.samhsa.c2s.pcm.service.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ConsentPdfGeneratorImpl implements ConsentPdfGenerator {
    private static final String CONSENT_PDF = "consent-pdf";
    private final PdfBoxService pdfBoxService;

    @Autowired
    public ConsentPdfGeneratorImpl(PdfBoxService pdfBoxService) {
        this.pdfBoxService = pdfBoxService;
    }

    @Override
    public byte[] generateConsentPdf(Consent consent, PatientDto patientProfile, boolean isSigned, Date attestedOn, String consentTerms, Optional<UserDto> attester) {
        pdfBoxService.getConfiguredPdfFont(CONSENT_PDF);
        return null;
    }
}
