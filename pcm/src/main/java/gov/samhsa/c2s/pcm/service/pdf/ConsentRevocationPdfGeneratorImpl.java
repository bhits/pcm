package gov.samhsa.c2s.pcm.service.pdf;

import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.TelecomDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.exception.InvalidContentException;
import gov.samhsa.c2s.pcm.infrastructure.exception.PdfGenerateException;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.Column;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxService;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxStyle;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.TableAttribute;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PdfBoxHandler;
import gov.samhsa.c2s.pcm.service.exception.NoDataFoundException;
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
    private final ConsentPdfGenerator consentPdfGenerator;

    @Autowired
    public ConsentRevocationPdfGeneratorImpl(PdfBoxService pdfBoxService, ConsentPdfGenerator consentPdfGenerator) {
        this.pdfBoxService = pdfBoxService;
        this.consentPdfGenerator = consentPdfGenerator;
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

            // Configure each drawing section yCoordinate in order to centralized adjust layout
            final float titleSectionStartYCoordinate = page.getMediaBox().getHeight() - PdfBoxStyle.TOP_BOTTOM_MARGINS_OF_LETTER;
            final float consentReferenceNumberSectionStartYCoordinate = 670f;
            final float consentRevocationTermsSectionStartYCoordinate = 600f;
            final float consentRevocationSigningSectionStartYCoordinate = 290f;

            // Title
            consentPdfGenerator.addConsentTitle(CONSENT_REVOCATION_PDF, titleSectionStartYCoordinate, page, contentStream);

            // Consent Reference Number and Patient information
            consentPdfGenerator.addConsentReferenceNumberAndPatientInfo(consent, patient, consentReferenceNumberSectionStartYCoordinate, defaultFont, contentStream);

            // Consent revocation terms
            addConsentRevocationTerms(consentRevocationTerm, consentRevocationTermsSectionStartYCoordinate, defaultFont, page, contentStream);

            // Revocation signing details
            if (revokedByPatient.orElseThrow(NoDataFoundException::new)) {
                // Consent is revoked by Patient
                addPatientRevocationSigningDetailsTable(patient, revokedOnDateTime, consentRevocationSigningSectionStartYCoordinate, contentStream);
            } else {
                // Consent is NOT revoked by Patient
                //Todo: Will identify different role once C2S support for multiple role.
                String role = "Provider";
                addNonPatientRevocationSigningDetailsTable(role, revokedByUserDto, revokedOnDateTime, consentRevocationSigningSectionStartYCoordinate, defaultFont, contentStream);
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

    private void addConsentRevocationTerms(String consentRevocationTerm, float startYCoordinate, PDFont defaultFont, PDPage page, PDPageContentStream contentStream) {
        try {
            pdfBoxService.addAutoWrapParagraphByPageWidth(consentRevocationTerm, defaultFont, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, startYCoordinate, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, page, contentStream);
        } catch (Exception e) {
            log.error("Invalid character for cast specification", e);
            throw new InvalidContentException(e);
        }
    }

    private void addPatientRevocationSigningDetailsTable(PatientDto patient, Date revokedOnDateTime, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
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

        generateSigningDetailsTable(tableContent, startYCoordinate, contentStream);
    }

    private void addNonPatientRevocationSigningDetailsTable(String role, Optional<UserDto> revokedByUserDto, Date revokedOnDateTime, float startYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
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

        generateSigningDetailsTable(tableContent, startYCoordinate, contentStream);

        final float signatureStartYCoordinate = 200f;
        generateRequestedSignatureTable(signatureStartYCoordinate, font, contentStream);
    }

    private void generateSigningDetailsTable(List<List<String>> tableContent, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
        final float columnWidth = 240f;
        final float cellMargin = 1f;

        Column column = new Column(columnWidth);
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER)
                .yCoordinate(startYCoordinate)
                .rowHeight(PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT)
                .cellMargin(cellMargin)
                .contentFont(PDType1Font.TIMES_BOLD)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Collections.singletonList(column))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void generateRequestedSignatureTable(float startYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float tableStartYCoordinate = startYCoordinate - PdfBoxStyle.SMALL_LINE_SPACE;
        final float columnWidth = 240f;
        final float cellMargin = 1f;

        String label = "Patient/Patient Representative:";
        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK,
                PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, startYCoordinate, contentStream);

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

        Column column = new Column(columnWidth);
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER)
                .yCoordinate(tableStartYCoordinate)
                .rowHeight(PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT)
                .cellMargin(cellMargin)
                .contentFont(font)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Collections.singletonList(column))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }
}
