package gov.samhsa.c2s.pcm.service.pdf;


import com.google.common.collect.ImmutableMap;
import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.SensitivityCategory;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConsentPdfGeneratorImpl implements ConsentPdfGenerator {
    private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy";
    private static final String CONSENT_PDF = "consent-pdf";
    private static final String TELECOM_EMAIL = "EMAIL";

    private final PdfBoxService pdfBoxService;
    private final PdfProperties pdfProperties;

    @Autowired
    public ConsentPdfGeneratorImpl(PdfBoxService pdfBoxService, PdfProperties pdfProperties) {
        this.pdfBoxService = pdfBoxService;
        this.pdfProperties = pdfProperties;
    }

    @Override
    public byte[] generateConsentPdf(Consent consent, PatientDto patientProfile, Date operatedOnDateTime, String consentTerms, Optional<UserDto> operatedByUserDto, Optional<Boolean> operatedByPatient) throws IOException {
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

            // Configure each drawing section yCoordinate in order to centralized adjust layout
            final float titleSectionStartYCoordinate = page.getMediaBox().getHeight() - PdfBoxStyle.TB_MARGINS_OF_LETTER;
            final float consentReferenceNumberSectionStartYCoordinate = 710f;
            final float authorizationSectionStartYCoordinate = 640f;
            final float healthInformationSectionStartYCoordinate = 455f;
            final float consentTermsSectionStartYCoordinate = 270f;
            final float consentEffectiveDateSectionStartYCoordinate = 130f;
            final float consentSigningSectionStartYCoordinate = 100f;

            // Title
            addConsentTitle(titleSectionStartYCoordinate, page, contentStream);

            // Consent Reference Number and Patient information
            addConsentReferenceNumberAndPatientInfo(consent, patientProfile, consentReferenceNumberSectionStartYCoordinate, contentStream);

            // Authorization to disclose section
            addAuthorizationToDisclose(consent, authorizationSectionStartYCoordinate, defaultFont, page, contentStream);

            // Health information to be disclosed section
            addHealthInformationToBeDisclose(consent, healthInformationSectionStartYCoordinate, defaultFont, page, contentStream);

            // Consent terms section
            addConsentTerms(consentTerms, patientProfile, consentTermsSectionStartYCoordinate, defaultFont, page, contentStream);

            // Consent effective and expiration date
            addEffectiveAndExpirationDate(consent, consentEffectiveDateSectionStartYCoordinate, contentStream);

            // Consent signing details
            if (consent.getConsentStage().equals(ConsentStage.SIGNED)) {
                addConsentSigningDetails(patientProfile, operatedByUserDto, operatedOnDateTime, operatedByPatient, consentSigningSectionStartYCoordinate, defaultFont, contentStream);
            }

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

    private void addConsentTitle(float startYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        String consentTitle = pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(CONSENT_PDF))
                .map(PdfProperties.PdfConfig::getTitle)
                .findAny()
                .orElseThrow(PdfConfigMissingException::new);

        float titleFontSize = 20f;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        Color titleColor = Color.BLACK;
        pdfBoxService.addCenteredTextAtOffset(consentTitle, titleFont, titleFontSize, titleColor, startYCoordinate, page, contentStream);
    }

    private void addConsentReferenceNumberAndPatientInfo(Consent consent, PatientDto patientProfile, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
        final float rowHeight = PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT;
        final float cellMargin = 1f;

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
                .yCoordinate(startYCoordinate)
                .rowHeight(rowHeight)
                .cellMargin(cellMargin)
                .contentFont(PDType1Font.TIMES_ROMAN)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Arrays.asList(column1, column2))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void addAuthorizationToDisclose(Consent consent, float startYCoordinate, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        final float cardXCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        final float colAWidth = 180f;
        final float colBWidth = 100f;
        final float colCWidth = 180f;
        final float colDWidth = 100f;
        String title = "AUTHORIZATION TO DISCLOSE";
        drawSectionHeader(title, cardXCoordinate, startYCoordinate, page, contentStream);

        List<Column> tableColumns = Arrays.asList(new Column(colAWidth), new Column(colBWidth), new Column(colCWidth), new Column(colDWidth));

        // Provider permitted to disclose
        float providerPermittedStartYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        addProviderPermittedToDisclose(consent, tableColumns, providerPermittedStartYCoordinate, font, contentStream);

        // Provider disclosure is made to
        float providerDisclosureIsMadeToStartYCoordinate = 540f;
        addProviderDisclosureIsMadeTo(consent, tableColumns, providerDisclosureIsMadeToStartYCoordinate, font, contentStream);
    }

    private void addProviderPermittedToDisclose(Consent consent, List<Column> tableColumns, float startYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float xCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        final float headerYCoordinate = startYCoordinate - PdfBoxStyle.MEDIUM_LINE_SPACE;
        String label = "Authorizes:";
        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_MEDIUM_SIZE, Color.BLACK, xCoordinate, startYCoordinate, contentStream);
        addAuthorizationTableHeader(headerYCoordinate, tableColumns, font, contentStream);
    }

    private void addProviderDisclosureIsMadeTo(Consent consent, List<Column> tableColumns, float startYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float xCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        final float headerYCoordinate = startYCoordinate - PdfBoxStyle.MEDIUM_LINE_SPACE;
        String label = "To disclose to:";
        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_MEDIUM_SIZE, Color.BLACK, xCoordinate, startYCoordinate, contentStream);
        addAuthorizationTableHeader(headerYCoordinate, tableColumns, font, contentStream);
    }

    private void addAuthorizationTableHeader(float headerYCoordinate, List<Column> columnsWidth, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float rowHeight = 15f;
        final float cellMargin = 1f;
        // Provider table header
        String a1 = "Provider Name";
        String b1 = "NPI Number";
        String c1 = "Address";
        String d1 = "Phone";
        List<String> tableHeader = Arrays.asList(a1, b1, c1, d1);
        List<List<String>> header = Collections.singletonList(tableHeader);

        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(headerYCoordinate)
                .rowHeight(rowHeight)
                .cellMargin(cellMargin)
                .contentFont(font)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(columnsWidth)
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, header);
    }

    private void addHealthInformationToBeDisclose(Consent consent, float startYCoordinate, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        float cardXCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        float labelYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;

        String title = "HEALTH INFORMATION TO BE DISCLOSED";
        drawSectionHeader(title, cardXCoordinate, startYCoordinate, page, contentStream);

        // Medical Information
        addMedicalInformation(consent, labelYCoordinate, font, contentStream);

        // Purposes of use
        addPurposeOfUse(consent, labelYCoordinate, font, contentStream);
    }

    private void addMedicalInformation(Consent consent, float labelYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float xCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        final float listWidth = 286f;
        final String itemMarkerSymbol = "-";
        float subLabelYCoordinate = labelYCoordinate - 15f;
        float listYCoordinate = labelYCoordinate - 20f;
        String label = "To SHARE the following medical information:";
        String subLabel = "Sensitivity Categories:";

        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, xCoordinate, labelYCoordinate, contentStream);
        pdfBoxService.addTextAtOffset(subLabel, font, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, xCoordinate, subLabelYCoordinate, contentStream);

        List<String> sensitivityCategories = consent.getShareSensitivityCategories().stream()
                .map(SensitivityCategory::getDisplay)
                .collect(Collectors.toList());

        pdfBoxService.addUnorderedListContent(sensitivityCategories, itemMarkerSymbol, xCoordinate, listYCoordinate, listWidth, font, PdfBoxStyle.TEXT_SMALL_SIZE, contentStream);
    }

    private void addPurposeOfUse(Consent consent, float labelYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float xCoordinate = 326f;
        final float listWidth = 280f;
        final String itemMarkerSymbol = "-";
        float listYCoordinate = labelYCoordinate - 5f;
        String label = "To SHARE for the following purpose(s):";

        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, xCoordinate, labelYCoordinate, contentStream);
        List<String> purposes = consent.getSharePurposes().stream()
                .map(Purpose::getDisplay)
                .collect(Collectors.toList());

        pdfBoxService.addUnorderedListContent(purposes, itemMarkerSymbol, xCoordinate, listYCoordinate, listWidth, font, PdfBoxStyle.TEXT_SMALL_SIZE, contentStream);
    }

    private void addConsentTerms(String consentTerms, PatientDto patientProfile, float startYCoordinate, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        float cardXCoordinate = PdfBoxStyle.LR_MARGINS_OF_LETTER;
        final float paragraphYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        final String title = "CONSENT TERMS";

        drawSectionHeader(title, cardXCoordinate, startYCoordinate, page, contentStream);

        final String userNameKey = "ATTESTER_FULL_NAME";
        String termsWithAttestedName = StrSubstitutor.replace(consentTerms,
                ImmutableMap.of(userNameKey, UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName())));

        try {
            pdfBoxService.addAutoWrapParagraphByPageWidth(termsWithAttestedName, font, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, paragraphYCoordinate, PdfBoxStyle.LR_MARGINS_OF_LETTER, page, contentStream);
        } catch (Exception e) {
            log.error("Invalid character for cast specification", e);
            throw new InvalidContentException(e);
        }
    }

    private void addEffectiveAndExpirationDate(Consent consent, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
        final float columnWidth = 180f;
        final float rowHeight = PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT;
        final float cellMargin = 1f;

        // Prepare table content
        String col1 = "Effective Date: ".concat(PdfBoxHandler.formatLocalDate(consent.getStartDate().toLocalDate(), DATE_FORMAT_PATTERN));
        String col2 = "Expiration Date: ".concat(PdfBoxHandler.formatLocalDate(consent.getEndDate().toLocalDate(), DATE_FORMAT_PATTERN));
        List<String> firstRowContent = Arrays.asList(col1, col2);

        List<List<String>> tableContent = Collections.singletonList(firstRowContent);

        // Config each column width
        Column column1 = new Column(columnWidth);
        Column column2 = new Column(columnWidth);

        // Config Table attribute
        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(startYCoordinate)
                .rowHeight(rowHeight)
                .cellMargin(cellMargin)
                .contentFont(PDType1Font.TIMES_BOLD)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(Arrays.asList(column1, column2))
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void addConsentSigningDetails(PatientDto patient, Optional<UserDto> signedByUserDto, Date signedOnDateTime, Optional<Boolean> signedByPatient, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException {
        // Consent signing details
        if (signedByPatient.orElseThrow(NoDataFoundException::new)) {
            // Consent is signed by Patient
            addPatientSigningDetailsTable(patient, signedOnDateTime, startYCoordinate, contentStream);
        } else {
            // Consent is NOT signed by Patient
            //Todo: Will identify different role once C2S support for multiple role.
            String role = "Provider";
            addNonPatientSigningDetailsTable(role, signedByUserDto, signedOnDateTime, startYCoordinate, contentStream);
        }
    }

    private void addPatientSigningDetailsTable(PatientDto patient, Date signedOnDateTime, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
        final float columnWidth = 240f;

        String patientName = UserInfoHelper.getFullName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
        String email = patient.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Prepare table content
        // First row
        String a1 = "Signed by: ".concat(patientName);
        List<String> firstRowContent = Collections.singletonList(a1);

        // Second row
        String a2 = "Email: ".concat(email);
        List<String> secondRowContent = Collections.singletonList(a2);
        // Third row
        String a3 = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN));
        List<String> thirdRowContent = Collections.singletonList(a3);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent, thirdRowContent);

        List<Column> columns = Collections.singletonList(new Column(columnWidth));
        generateSigningDetailsTable(columns, tableContent, startYCoordinate, contentStream);
    }

    private void addNonPatientSigningDetailsTable(String role, Optional<UserDto> signedByUserDto, Date signedOnDateTime, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
        final float columnWidth = 286f;

        UserDto signedUser = signedByUserDto.orElseThrow(NoDataFoundException::new);
        String userFullName = UserInfoHelper.getUserFullName(signedUser);
        String email = signedUser.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Prepare table content
        // First row
        String a1 = "Signed by ".concat(role.substring(0, 1).toUpperCase() + role.substring(1) + ": ").concat(userFullName);
        String b1 = "Patient/Patient Representative:";
        List<String> firstRowContent = Arrays.asList(a1, b1);

        // Second row
        String a2 = "Email: ".concat(email);
        String b2 = "Signature: __________________________";
        List<String> secondRowContent = Arrays.asList(a2, b2);

        // Third row
        String a3 = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN));
        String b3 = "Print Name: _________________________";
        List<String> thirdRowContent = Arrays.asList(a3, b3);

        // Forth row
        String b4 = "Date: _______________________________";
        List<String> forthRowContent = Collections.singletonList(b4);

        List<List<String>> tableContent = Arrays.asList(firstRowContent, secondRowContent, thirdRowContent, forthRowContent);

        List<Column> columns = Arrays.asList(new Column(columnWidth), new Column(columnWidth));
        generateSigningDetailsTable(columns, tableContent, startYCoordinate, contentStream);
    }

    private void generateSigningDetailsTable(List<Column> columns, List<List<String>> tableContent, float startYCoordinate, PDPageContentStream contentStream) throws IOException {
        final float rowHeight = PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT;
        final float cellMargin = 1f;

        TableAttribute tableAttribute = TableAttribute.builder()
                .xCoordinate(PdfBoxStyle.LR_MARGINS_OF_LETTER)
                .yCoordinate(startYCoordinate)
                .rowHeight(rowHeight)
                .cellMargin(cellMargin)
                .contentFont(PDType1Font.TIMES_BOLD)
                .contentFontSize(PdfBoxStyle.TEXT_SMALL_SIZE)
                .borderColor(Color.WHITE)
                .columns(columns)
                .build();

        pdfBoxService.addTableContent(contentStream, tableAttribute, tableContent);
    }

    private void drawSectionHeader(String title, float cardXCoordinate, float cardYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        // Set background color
        Color color = new Color(73, 89, 105);
        float colorBoxWidth = page.getMediaBox().getWidth() - 2 * PdfBoxStyle.LR_MARGINS_OF_LETTER;
        float colorBoxHeight = PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        float titleFontSize = PdfBoxStyle.TEXT_MEDIUM_SIZE;
        Color titleColor = Color.WHITE;

        pdfBoxService.addColorBox(color, cardXCoordinate, cardYCoordinate, colorBoxWidth, colorBoxHeight, page, contentStream);

        float titleYCoordinate = cardYCoordinate + (colorBoxHeight / 2)
                - ((titleFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * titleFontSize) / 4);

        pdfBoxService.addTextAtOffset(title, titleFont, titleFontSize, titleColor, cardXCoordinate + 4f, titleYCoordinate, contentStream);
    }
}
