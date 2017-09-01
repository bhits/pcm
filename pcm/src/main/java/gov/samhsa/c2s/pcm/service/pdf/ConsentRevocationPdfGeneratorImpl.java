package gov.samhsa.c2s.pcm.service.pdf;

import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.TelecomDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.exception.InvalidContentException;
import gov.samhsa.c2s.pcm.infrastructure.exception.PdfGenerateException;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.Column;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxService;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.TableAttribute;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PdfBoxHandler;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxStyle;
import gov.samhsa.c2s.pcm.service.exception.NoDataFoundException;
import gov.samhsa.c2s.pcm.service.exception.PdfConfigMissingException;
import gov.samhsa.c2s.pcm.service.util.UserInfoHelper;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConsentRevocationPdfGeneratorImpl implements ConsentRevocationPdfGenerator {
    private static final String TELECOM_EMAIL = "EMAIL";
    private static final String CONSENT_REVOCATION_PDF = "consent-revocation-pdf";
    private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy";

    private final PdfBoxService pdfBoxService;
    private final PdfProperties pdfProperties;

    @Autowired
    public ConsentRevocationPdfGeneratorImpl(PdfBoxService pdfBoxService, PdfProperties pdfProperties) {
        this.pdfBoxService = pdfBoxService;
        this.pdfProperties = pdfProperties;
    }

    @Override
    public byte[] generateConsentRevocationPdf(Consent consent, PatientDto patient, Date revokedOnDateTime, String consentRevocationTerm, Optional<UserDto> revokedByUserDto, Optional<Boolean> revokedByPatient) throws IOException {
        Assert.notNull(consent, "Consent is required.");

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        // Create a new empty document
        PDDocument document = new PDDocument();

        // Create a new blank page with configured page size and add it to the document
        PDPage page = pdfBoxService.generatePage(CONSENT_REVOCATION_PDF, document);
        log.debug("Configured page size is: " + pdfBoxService.getConfiguredPdfFont(CONSENT_REVOCATION_PDF));

        // Set configured font
        PDFont defaultFont = pdfBoxService.getConfiguredPdfFont(CONSENT_REVOCATION_PDF);
        log.debug("Configured font is: " + defaultFont);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // Title
            addConsentRevocationTitle(page, contentStream);

            // Consent Reference Number and Patient information
            addConsentReferenceNumberAndPatientInfo(consent, patient, contentStream);

            // Consent revocation terms
            addConsentRevocationTerms(consentRevocationTerm, defaultFont, page, contentStream);

            // Revocation signing details
            if (revokedByPatient.orElseThrow(NoDataFoundException::new)) {
                // Consent is revoked by Patient
                addPatientRevocationSigningDetailsTable(patient, revokedOnDateTime, contentStream);
            } else {
                // Consent is NOT revoked by Patient
                //Todo: Will identify different role once C2S support for multiple role.
                String role = "Provider";
                addNonPatientRevocationSigningDetailsTable(role, revokedByUserDto, revokedOnDateTime, defaultFont, contentStream);
            }

            // Make sure that the content stream is closed
            contentStream.close();

            //Save the document to an output stream
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

    private void addConsentRevocationTitle(PDPage page, PDPageContentStream contentStream) throws IOException {
        String consentRevocationTitle = pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(CONSENT_REVOCATION_PDF))
                .map(PdfProperties.PdfConfig::getTitle)
                .findAny()
                .orElseThrow(PdfConfigMissingException::new);

        float titleFontSize = 20f;
        float yCoordinate = page.getMediaBox().getHeight() - PdfBoxStyle.TB_MARGINS_OF_LETTER;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        Color titleColor = Color.BLACK;
        pdfBoxService.addCenteredTextAtOffset(consentRevocationTitle, titleFont, titleFontSize, titleColor, yCoordinate, page, contentStream);
    }

    private void addConsentReferenceNumberAndPatientInfo(Consent consent, PatientDto patient, PDPageContentStream contentStream) throws IOException {
        String consentReferenceNumber = consent.getConsentReferenceId();
        String patientFullName = UserInfoHelper.getFullName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
        String patientBirthDate = PdfBoxHandler.formatLocalDate(patient.getBirthDate(), DATE_FORMAT_PATTERN);

        // Prepare table content
        // First row
        String a1 = "Consent Reference Number: ".concat(consentReferenceNumber);
        List<String> firstRowContent = Arrays.asList(a1, null);

        // Second row
        String a2 = "Patient Name: ".concat(patientFullName);
        String b2 = "Patient DOB: ".concat(patientBirthDate);
        List<String> secondRowContent = Arrays.asList(a2, b2);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent);

        // Config each column width
        Column column1 = new Column(240f);
        Column column2 = new Column(220f);

        // Config Table attribute
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(670f)
                .rowHeight(30f)
                .cellMargin(1f)
                .contentFont(PDType1Font.TIMES_ROMAN)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Arrays.asList(column1, column2))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void addConsentRevocationTerms(String consentRevocationTerm, PDFont defaultFont, PDPage page, PDPageContentStream contentStream) {
        try {
            pdfBoxService.addAutoWrapParagraphByPageWidth(consentRevocationTerm, defaultFont, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, 580f, PdfBoxStyle.LR_MARGINS_OF_LETTER, page, contentStream);
        } catch (Exception e) {
            log.error("Invalid character for cast specification", e);
            throw new InvalidContentException(e);
        }
    }

    private void addPatientRevocationSigningDetailsTable(PatientDto patient, Date revokedOnDateTime, PDPageContentStream contentStream) throws IOException {
        String patientName = UserInfoHelper.getFullName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
        String email = patient.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate revokedDate = revokedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Prepare table content
        // First row
        String a1 = "Signed by: ".concat(patientName);
        List<String> firstRowContent = Collections.singletonList(a1);

        // Second row
        String a2 = "Email: ".concat(email);
        List<String> secondRowContent = Collections.singletonList(a2);
        // Third row
        String a3 = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(revokedDate, DATE_FORMAT_PATTERN));
        List<String> thirdRowContent = Collections.singletonList(a3);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent, thirdRowContent);

        generateSigningDetailsTable(tableContent, contentStream);
    }

    private void addNonPatientRevocationSigningDetailsTable(String role, Optional<UserDto> revokedByUserDto, Date revokedOnDateTime, PDFont font, PDPageContentStream contentStream) throws IOException {
        UserDto revokedUser = revokedByUserDto.orElseThrow(NoDataFoundException::new);
        String userFullName = UserInfoHelper.getUserFullName(revokedUser);
        String email = revokedUser.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate revokedDate = revokedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Prepare table content
        // First row
        String a1 = "Signed by ".concat(role.substring(0, 1).toUpperCase() + role.substring(1) + ": ").concat(userFullName);
        List<String> firstRowContent = Collections.singletonList(a1);

        // Second row
        String a2 = "Email: ".concat(email);
        List<String> secondRowContent = Collections.singletonList(a2);
        // Third row
        String a3 = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(revokedDate, DATE_FORMAT_PATTERN));
        List<String> thirdRowContent = Collections.singletonList(a3);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent, thirdRowContent);

        generateSigningDetailsTable(tableContent, contentStream);

        generateRequestedSignatureTable(font, contentStream);
    }

    private void generateSigningDetailsTable(List<List<String>> tableContent, PDPageContentStream contentStream) throws IOException {
        Column column = new Column(240f);
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(290f)
                .rowHeight(PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT)
                .cellMargin(1f)
                .contentFont(PDType1Font.TIMES_BOLD)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Collections.singletonList(column))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void generateRequestedSignatureTable(PDFont font, PDPageContentStream contentStream) throws IOException {
        String label = "Patient/Patient Representative:";
        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK,
                PdfBoxStyle.LR_MARGINS_OF_LETTER, 200f, contentStream);

        // Prepare table content
        // First row
        String a1 = "Signature: __________________________";
        List<String> firstRowContent = Collections.singletonList(a1);

        // Second row
        String a2 = "Print Name: _________________________";
        List<String> secondRowContent = Collections.singletonList(a2);
        // Third row
        String a3 = "Date: _______________________________";
        List<String> thirdRowContent = Collections.singletonList(a3);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent, thirdRowContent);

        Column column = new Column(240f);
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(195f)
                .rowHeight(PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT)
                .cellMargin(1f)
                .contentFont(font)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Collections.singletonList(column))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }
}
