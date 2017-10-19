package gov.samhsa.c2s.pcm.service.pdf;

import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxService;
import gov.samhsa.c2s.pcm.service.pdf.hexPdf.HexPDF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class ConsentRevocationPdfGeneratorImpl implements ConsentRevocationPdfGenerator {
    private static final String CONSENT_REVOCATION_PDF = "consent-revocation-pdf";

    private final PdfBoxService pdfBoxService;
    private final ConsentPdfGenerator consentPdfGenerator;

    private HexPDF document;

    @Autowired
    public ConsentRevocationPdfGeneratorImpl(PdfBoxService pdfBoxService, ConsentPdfGenerator consentPdfGenerator) {
        this.pdfBoxService = pdfBoxService;
        this.consentPdfGenerator = consentPdfGenerator;
    }

    @Override
    public byte[] generateConsentRevocationPdf(Consent consent, PatientDto patient, Date revokedOnDateTime, String consentRevocationTerm, Optional<UserDto> revokedByUserDto, Optional<Boolean> revokedByPatient) throws IOException {

        Assert.notNull(consent, "Consent is required.");

        document = new HexPDF();

        String consentTitle = consentPdfGenerator.getConsentTitle(CONSENT_REVOCATION_PDF);

        consentPdfGenerator.setPageFooter(document, consentTitle);

        // Create the first page
        document.newPage();

        // Set document title
        consentPdfGenerator.drawConsentTitle(document, consentTitle);

        // Typeset everything else in boring black
        document.setTextColor(Color.black);

        document.normalStyle();

        consentPdfGenerator.drawPatientInformationSection(document, consent, patient);

        document.drawText("\n");

        document.drawText(consentRevocationTerm);

        document.drawText("\n\n");

        consentPdfGenerator.addConsentSigningDetails(document, patient, revokedByUserDto, revokedOnDateTime, revokedByPatient);

        // Get the document
        return document.getDocumentAsBytArray();
    }
}
