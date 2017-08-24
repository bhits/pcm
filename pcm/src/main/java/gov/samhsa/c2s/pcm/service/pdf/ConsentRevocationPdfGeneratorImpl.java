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
public class ConsentRevocationPdfGeneratorImpl implements ConsentRevocationPdfGenerator {
    private static final String CONSENT_REVOCATION_PDF = "consent-revocation-pdf";
    private final PdfBoxService pdfBoxService;
    private final String EMAIL = "EMAIL";

    @Autowired
    public ConsentRevocationPdfGeneratorImpl(PdfBoxService pdfBoxService) {
        this.pdfBoxService = pdfBoxService;
    }

    @Override
    public byte[] generateConsentRevocationPdf(Consent consent, PatientDto patient, Date attestedOnDateTime, String consentRevocationTerm, Optional<UserDto> revokedByUserDto) {
        pdfBoxService.getConfiguredPdfFont(CONSENT_REVOCATION_PDF);
        return null;
    }
}
