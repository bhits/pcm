package gov.samhsa.c2s.pcm.service.pdf;


import com.google.common.collect.ImmutableMap;
import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.exception.InvalidContentException;
import gov.samhsa.c2s.pcm.infrastructure.exception.PdfGenerateException;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.Column;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxService;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.TableAttribute;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PdfBoxHandler;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PdfBoxStyle;
import gov.samhsa.c2s.pcm.service.exception.PdfConfigMissingException;
import gov.samhsa.c2s.pcm.service.util.UserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConsentPdfGeneratorImpl implements ConsentPdfGenerator {
    private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy";
    private static final String CONSENT_PDF = "consent-pdf";

    private final PdfBoxService pdfBoxService;
    private final PdfProperties pdfProperties;

    @Autowired
    public ConsentPdfGeneratorImpl(PdfBoxService pdfBoxService, PdfProperties pdfProperties) {
        this.pdfBoxService = pdfBoxService;
        this.pdfProperties = pdfProperties;
    }

    @Override
    public byte[] generateConsentPdf(Consent consent, PatientDto patientProfile, boolean isSigned, Date attestedOn, String consentTerms, Optional<UserDto> attester, Optional<Boolean> operatedByPatient) throws IOException {
        Assert.notNull(consent, "Consent is required.");

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        // Create a new empty document
        PDDocument document = new PDDocument();

        // Create a new blank page with configured page size and add it to the document
        PDPage page = pdfBoxService.generatePage(CONSENT_PDF, document);
        log.debug("Configured page size is: " + pdfBoxService.getConfiguredPdfFont(CONSENT_PDF));

        // Set configured font
        PDFont defaultFont = pdfBoxService.getConfiguredPdfFont(CONSENT_PDF);
        log.debug("Configured font is: " + defaultFont);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // Title
            addConsentTitle(page, contentStream);

            // Consent Reference Number and Patient information
            addConsentReferenceNumberAndPatientInfo(consent, patientProfile, contentStream);

            // Authorization to disclose section
            addAuthorizationToDisclose(consent, page, contentStream);
            // Health information to be disclosed section

            // Consent terms section
            addConsentTerms(consentTerms, patientProfile, defaultFont, page, contentStream);

            // Consent effective and expiration date
            addEffectiveAndExpirationDate(consent, contentStream);

            // Consent signing details

            // Make sure that the content stream is closed
            contentStream.close();

            // Save the document to an output stream
            document.save(pdfOutputStream);

            return pdfOutputStream.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new PdfGenerateException(e);
        } finally {
            pdfOutputStream.close();
            // finally make sure that the document is properly closed
            document.close();
        }
    }

    private void addConsentTitle(PDPage page, PDPageContentStream contentStream) throws IOException {
        String consentTitle = pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(CONSENT_PDF))
                .map(PdfProperties.PdfConfig::getTitle)
                .findAny()
                .orElseThrow(PdfConfigMissingException::new);

        float titleFontSize = 20f;
        float yCoordinate = page.getMediaBox().getHeight() - PdfBoxStyle.TB_MARGINS_OF_LETTER;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        Color titleColor = Color.BLACK;
        pdfBoxService.addCenteredTextAtOffset(consentTitle, titleFont, titleFontSize, titleColor, yCoordinate, page, contentStream);
    }

    private void addConsentReferenceNumberAndPatientInfo(Consent consent, PatientDto patientProfile, PDPageContentStream contentStream) throws IOException {
        String consentReferenceNumber = consent.getConsentReferenceId();
        String patientFullName = UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName());
        String patientBirthDate = PdfBoxHandler.formatLocalDate(patientProfile.getBirthDate(), DATE_FORMAT_PATTERN);

        // Prepare table content
        // First row
        String r1 = "Consent Reference Number: ".concat(consentReferenceNumber);
        List<String> firstRowContent = Arrays.asList(r1, null);

        // Second row
        String r3 = "Patient Name: ".concat(patientFullName);
        String r4 = "Patient DOB: ".concat(patientBirthDate);
        List<String> secondRowContent = Arrays.asList(r3, r4);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent);

        // Config each column width
        Column column1 = new Column(240f);
        Column column2 = new Column(220f);

        // Config Table attribute
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(700f)
                .rowHeight(20f)
                .cellMargin(1f)
                .contentFont(PDType1Font.TIMES_ROMAN)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Arrays.asList(column1, column2))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void addAuthorizationToDisclose(Consent consent, PDPage page, PDPageContentStream contentStream) throws IOException {
        float cardXCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        float cardYCoordinate = 630f;
        String title = "AUTHORIZATION TO DISCLOSE";
        drawSectionHeader(title, cardXCoordinate, cardYCoordinate, page, contentStream);

        // Authorizes label
        pdfBoxService.addTextAtOffset("Authorizes:", PDType1Font.TIMES_ROMAN, PdfBoxStyle.TEXT_MEDIUM_SIZE, Color.BLACK, PdfBoxStyle.LR_MARGINS_OF_LETTER, 610f, contentStream);
        // From providers table
        createProviderPermittedToDiscloseTable(consent, contentStream);
        // Disclose label
        pdfBoxService.addTextAtOffset("To disclose to:", PDType1Font.TIMES_ROMAN, PdfBoxStyle.TEXT_MEDIUM_SIZE, Color.BLACK, PdfBoxStyle.LR_MARGINS_OF_LETTER, 530f, contentStream);

    }

    private void drawSectionHeader(String title, float cardXCoordinate, float cardYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        // Set background color
        Color color = new Color(73, 89, 105);
        float colorBoxWidth = page.getMediaBox().getWidth() - 2 * PdfBoxStyle.LR_MARGINS_OF_LETTER;
        float colorBoxHeight = 20f;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        float titleFontSize = PdfBoxStyle.TEXT_MEDIUM_SIZE;
        Color titleColor = Color.WHITE;

        pdfBoxService.addColorBox(color, cardXCoordinate, cardYCoordinate, colorBoxWidth, colorBoxHeight, page, contentStream);

        float titleYCoordinate = cardYCoordinate + (colorBoxHeight / 2)
                - ((titleFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * titleFontSize) / 4);

        pdfBoxService.addTextAtOffset(title, titleFont, titleFontSize, titleColor, cardXCoordinate + 4f, titleYCoordinate, contentStream);
    }

    private void createProviderPermittedToDiscloseTable(Consent consent, PDPageContentStream contentStream) {
    }

    private void addConsentTerms(String consentTerms, PatientDto patientProfile, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        float cardXCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        float cardYCoordinate = 270f;
        final String title = "CONSENT TERMS";
        drawSectionHeader(title, cardXCoordinate, cardYCoordinate, page, contentStream);

        final String userNameKey = "ATTESTER_FULL_NAME";
        String termsWithAttestedName = StrSubstitutor.replace(consentTerms,
                ImmutableMap.of(userNameKey, UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName())));

        try {
            pdfBoxService.addAutoWrapParagraphByPageWidth(termsWithAttestedName, font, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, 250f, PdfBoxStyle.LR_MARGINS_OF_LETTER, page, contentStream);
        } catch (Exception e) {
            log.error("Invalid character for cast specification", e);
            throw new InvalidContentException(e);
        }
    }

    private void addEffectiveAndExpirationDate(Consent consent, PDPageContentStream contentStream) throws IOException {
        // Prepare table content
        String col1 = "Effective Date: ".concat(PdfBoxHandler.formatLocalDate(consent.getStartDate().toLocalDate(), DATE_FORMAT_PATTERN));
        String col2 = "Expiration Date: ".concat(PdfBoxHandler.formatLocalDate(consent.getEndDate().toLocalDate(), DATE_FORMAT_PATTERN));
        List<String> firstRowContent = Arrays.asList(col1, col2);

        List<List<String>> tableContent = Arrays.asList(firstRowContent);

        // Config each column width
        Column column1 = new Column(180f);
        Column column2 = new Column(180f);

        // Config Table attribute
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(180f)
                .rowHeight(20f)
                .cellMargin(1f)
                .contentFont(PDType1Font.TIMES_BOLD)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Arrays.asList(column1, column2))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }
}
