package gov.samhsa.c2s.pcm.service.pdf;


import com.google.common.collect.ImmutableMap;
import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.domain.Organization;
import gov.samhsa.c2s.pcm.domain.Practitioner;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.SensitivityCategory;
import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.TelecomDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.exception.InvalidContentException;
import gov.samhsa.c2s.pcm.infrastructure.exception.PdfGenerationException;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.Column;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxService;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxStyle;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.TableAttribute;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.TextAlignment;
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
    private final PlsService plsService;

    @Autowired
    public ConsentPdfGeneratorImpl(PdfBoxService pdfBoxService, PdfProperties pdfProperties, PlsService plsService) {
        this.pdfBoxService = pdfBoxService;
        this.pdfProperties = pdfProperties;
        this.plsService = plsService;
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
            //TODO: Enable to support relative layout

            // Configure each drawing section yCoordinate in order to centralized adjust layout
            final float titleSectionStartYCoordinate = page.getMediaBox().getHeight() - PdfBoxStyle.TOP_BOTTOM_MARGINS_OF_LETTER;
            final float consentReferenceNumberSectionStartYCoordinate = 690f;
            final float authorizationSectionStartYCoordinate = 640f;
            final float healthInformationSectionStartYCoordinate = 455f;
            final float consentTermsSectionStartYCoordinate = 270f;
            final float consentEffectiveDateSectionStartYCoordinate = 125f;
            final float consentSigningSectionStartYCoordinate = 90f;

            // Title
            addConsentTitle(CONSENT_PDF, titleSectionStartYCoordinate, page, contentStream);

            // Consent Reference Number and Patient information
            addConsentReferenceNumberAndPatientInfo(consent, patientProfile, consentReferenceNumberSectionStartYCoordinate, defaultFont, contentStream);

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
            throw new PdfGenerationException(e);
        } finally {
            pdfOutputStream.close();
            // finally make sure that the document is properly closed
            document.close();
        }
    }

    @Override
    public void addConsentTitle(String pdfType, float startYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        String consentTitle = pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(pdfType))
                .map(PdfProperties.PdfConfig::getTitle)
                .findAny()
                .orElseThrow(PdfConfigMissingException::new);

        float titleFontSize = 20f;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        Color titleColor = Color.BLACK;
        pdfBoxService.addCenteredTextAtOffset(consentTitle, titleFont, titleFontSize, titleColor, startYCoordinate, page, contentStream);
    }

    @Override
    public void addConsentReferenceNumberAndPatientInfo(Consent consent, PatientDto patientProfile, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException {
        String consentReferenceNumber = consent.getConsentReferenceId();
        String patientFullName = UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName());
        String patientBirthDate = PdfBoxHandler.formatLocalDate(patientProfile.getBirthDate(), DATE_FORMAT_PATTERN);

        final Color textColor = Color.BLACK;
        final float fontSize = PdfBoxStyle.TEXT_SMALL_SIZE;
        final PDFont contentFont = PDType1Font.TIMES_BOLD;

        // Add Consent Reference Number
        final String crnLabel = "Consent Reference Number: ";
        pdfBoxService.addTextAtOffset(crnLabel, defaultFont, fontSize, textColor, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, startYCoordinate, contentStream);
        final float crnXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + PdfBoxHandler.targetedStringWidth(crnLabel, defaultFont, fontSize);
        pdfBoxService.addTextAtOffset(consentReferenceNumber, contentFont, fontSize, textColor, crnXCoordinate, startYCoordinate, contentStream);

        // Add patient name
        final float nameYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        final String nameLabel = "Patient Name: ";
        pdfBoxService.addTextAtOffset(nameLabel, defaultFont, fontSize, textColor, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, nameYCoordinate, contentStream);
        final float nameXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + PdfBoxHandler.targetedStringWidth(nameLabel, defaultFont, fontSize);
        pdfBoxService.addTextAtOffset(patientFullName, contentFont, fontSize, textColor, nameXCoordinate, nameYCoordinate, contentStream);

        // Add patient DOB
        final String dobLabel = "Patient DOB: ";
        final float dobLabelXCoordinate = 300f;
        pdfBoxService.addTextAtOffset(dobLabel, defaultFont, fontSize, textColor, dobLabelXCoordinate, nameYCoordinate, contentStream);
        final float dobXCoordinate = dobLabelXCoordinate + PdfBoxHandler.targetedStringWidth(dobLabel, defaultFont, fontSize);
        pdfBoxService.addTextAtOffset(patientBirthDate, contentFont, fontSize, textColor, dobXCoordinate, nameYCoordinate, contentStream);
    }

    @Override
    public void addConsentSigningDetails(PatientDto patient, Optional<UserDto> signedByUserDto, Date signedOnDateTime, Optional<Boolean> signedByPatient, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException {
        // Consent signing details
        if (signedByPatient.orElseThrow(NoDataFoundException::new)) {
            // Consent is signed by Patient
            addPatientSigningDetails(patient, signedOnDateTime, startYCoordinate, defaultFont, contentStream);
        } else {
            // Consent is NOT signed by Patient
            //Todo: Will identify different role once C2S support for multiple role.
            String role = "Provider";
            addNonPatientSigningDetails(role, signedByUserDto, signedOnDateTime, startYCoordinate, defaultFont, contentStream);
        }
    }

    private void addAuthorizationToDisclose(Consent consent, float startYCoordinate, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        final float cardXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER;
        final float colAWidth = 180f;
        final float colBWidth = 90f;
        final float colCWidth = 200f;
        final float colDWidth = 90f;
        String title = "AUTHORIZATION TO DISCLOSE";
        drawSectionHeader(title, cardXCoordinate, startYCoordinate, page, contentStream);

        List<Column> tableColumns = Arrays.asList(new Column(colAWidth), new Column(colBWidth), new Column(colCWidth), new Column(colDWidth));

        // Provider permitted to disclose
        float providerPermittedStartYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        addProviderPermittedToDisclose(consent, tableColumns, providerPermittedStartYCoordinate, font, page, contentStream);

        // Provider disclosure is made to
        float providerDisclosureIsMadeToStartYCoordinate = 535f;
        addProviderDisclosureIsMadeTo(consent, tableColumns, providerDisclosureIsMadeToStartYCoordinate, font, page, contentStream);
    }

    private void addProviderPermittedToDisclose(Consent consent, List<Column> tableColumns, float startYCoordinate, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        String label = "Authorizes:";
        addAuthorizationTableHeader(label, startYCoordinate, tableColumns, font, contentStream);

        // From providers details
        final float fromProviderDetailsYCoordinate = 207f;
        if (consent.getConsentStage().equals(ConsentStage.SIGNED)) {
            List<Organization> fromOrganizations = consent.getConsentAttestation().getFromOrganizations();
            List<Practitioner> fromPractitioners = consent.getConsentAttestation().getFromPractitioners();
            addSignedConsentProvidersDetails(fromOrganizations, fromPractitioners, tableColumns, fromProviderDetailsYCoordinate, page, contentStream);
        } else {
            List<Provider> fromProviders = consent.getFromProviders();
            addSavedConsentProvidersDetails(fromProviders, tableColumns, fromProviderDetailsYCoordinate, page, contentStream);
        }
    }

    private void addProviderDisclosureIsMadeTo(Consent consent, List<Column> tableColumns, float startYCoordinate, PDFont font, PDPage page, PDPageContentStream contentStream) throws IOException {
        String label = "To disclose to:";
        addAuthorizationTableHeader(label, startYCoordinate, tableColumns, font, contentStream);

        // To providers details
        final float toProviderDetailsYCoordinate = 292f;
        if (consent.getConsentStage().equals(ConsentStage.SIGNED)) {
            List<Organization> toOrganizations = consent.getConsentAttestation().getToOrganizations();
            List<Practitioner> toPractitioners = consent.getConsentAttestation().getToPractitioners();
            addSignedConsentProvidersDetails(toOrganizations, toPractitioners, tableColumns, toProviderDetailsYCoordinate, page, contentStream);
        } else {
            List<Provider> toProviders = consent.getToProviders();
            addSavedConsentProvidersDetails(toProviders, tableColumns, toProviderDetailsYCoordinate, page, contentStream);
        }
    }

    private void addSavedConsentProvidersDetails(List<Provider> providers, List<Column> columns, float providerDetailsYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        List<FlattenedSmallProviderDto> fromProviders = providers.stream()
                .map(provider -> plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER))
                .collect(Collectors.toList());

        float providerNameColWidth = columns.get(0).getCellWidth();
        float providerNPIColWidth = columns.get(1).getCellWidth();
        float providerAddressColWidth = columns.get(2).getCellWidth();
        float providerPhoneColWidth = columns.get(3).getCellWidth();

        for (FlattenedSmallProviderDto fromProvider : fromProviders) {
            // Provider Name
            drawProviderDetails(determineProviderName(fromProvider), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER,
                    providerDetailsYCoordinate, providerNameColWidth, page, contentStream);
            // Provider NPI Number
            drawProviderDetails(fromProvider.getNpi(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth,
                    providerDetailsYCoordinate, providerNPIColWidth, page, contentStream);
            // Provider Address
            drawProviderDetails(composeAddress(fromProvider), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth + providerNPIColWidth,
                    providerDetailsYCoordinate, providerAddressColWidth, page, contentStream);
            // Provider Phone
            drawProviderDetails(fromProvider.getPracticeLocationAddressTelephoneNumber(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth + providerNPIColWidth + providerAddressColWidth,
                    providerDetailsYCoordinate, providerPhoneColWidth, page, contentStream);
        }
    }

    private void addSignedConsentProvidersDetails(List<Organization> organizations, List<Practitioner> practitioners, List<Column> columns, float startYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        float providerNameColWidth = columns.get(0).getCellWidth();
        float providerNPIColWidth = columns.get(1).getCellWidth();
        float providerAddressColWidth = columns.get(2).getCellWidth();
        float providerPhoneColWidth = columns.get(3).getCellWidth();

        for (Practitioner practitioner : practitioners) {
            // Provider Name
            drawProviderDetails(UserInfoHelper.getFullName(practitioner.getFirstName(),
                    practitioner.getMiddleName(), practitioner.getLastName()), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER,
                    startYCoordinate, providerNameColWidth, page, contentStream);
            // Provider NPI Number
            drawProviderDetails(practitioner.getProvider().getIdentifier().getValue(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth,
                    startYCoordinate, providerNPIColWidth, page, contentStream);
            // Provider Address
            drawProviderDetails(composeAddress(practitioner.getAddress()), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth + providerNPIColWidth,
                    startYCoordinate, providerAddressColWidth, page, contentStream);
            // Provider Phone
            drawProviderDetails(practitioner.getPhoneNumber(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth + providerNPIColWidth + providerAddressColWidth,
                    startYCoordinate, providerPhoneColWidth, page, contentStream);
        }

        for (Organization organization : organizations) {
            // Provider Name
            drawProviderDetails(organization.getName(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER,
                    startYCoordinate, providerNameColWidth, page, contentStream);
            // Provider NPI Number
            drawProviderDetails(organization.getProvider().getIdentifier().getValue(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth,
                    startYCoordinate, providerNPIColWidth, page, contentStream);
            // Provider Address
            drawProviderDetails(composeAddress(organization.getAddress()), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth + providerNPIColWidth,
                    startYCoordinate, providerAddressColWidth, page, contentStream);
            // Provider Phone
            drawProviderDetails(organization.getPhoneNumber(), PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + providerNameColWidth + providerNPIColWidth + providerAddressColWidth,
                    startYCoordinate, providerPhoneColWidth, page, contentStream);
        }
    }

    private void drawProviderDetails(String providerInfo, float startXCoordinate, float startYCoordinate, float width, PDPage page, PDPageContentStream contentStream) throws IOException {
        final PDFont font = PDType1Font.TIMES_BOLD;
        final float fontSize = PdfBoxStyle.TEXT_SMALL_SIZE;
        final Color textColor = Color.BLACK;
        pdfBoxService.addWrappedParagraph(providerInfo, font, fontSize, textColor, TextAlignment.LEFT, startXCoordinate, startYCoordinate, width, page, contentStream);
    }

    private void addAuthorizationTableHeader(String label, float labelYCoordinate, List<Column> columnsWidth, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float xCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER;
        final float headerYCoordinate = labelYCoordinate - PdfBoxStyle.SMALL_LINE_SPACE;
        pdfBoxService.addTextAtOffset(label, PDType1Font.TIMES_BOLD, PdfBoxStyle.TEXT_MEDIUM_SIZE, Color.BLACK, xCoordinate, labelYCoordinate, contentStream);

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
                .xCoordinate(PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER)
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
        float cardXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER;
        float labelYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;

        String title = "HEALTH INFORMATION TO BE DISCLOSED";
        drawSectionHeader(title, cardXCoordinate, startYCoordinate, page, contentStream);

        // Medical Information
        addMedicalInformation(consent, labelYCoordinate, font, contentStream);

        // Purposes of use
        addPurposeOfUse(consent, labelYCoordinate, font, contentStream);
    }

    private void addMedicalInformation(Consent consent, float labelYCoordinate, PDFont font, PDPageContentStream contentStream) throws IOException {
        final float xCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER;
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
        float cardXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER;
        final float paragraphYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        final String title = "CONSENT TERMS";

        drawSectionHeader(title, cardXCoordinate, startYCoordinate, page, contentStream);

        final String userNameKey = "ATTESTER_FULL_NAME";
        String termsWithAttestedName = StrSubstitutor.replace(consentTerms,
                ImmutableMap.of(userNameKey, UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName())));

        try {
            pdfBoxService.addWrappedParagraphByLineBreaks(termsWithAttestedName, font, PdfBoxStyle.TEXT_SMALL_SIZE, Color.BLACK, paragraphYCoordinate, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, page, contentStream);
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
                .xCoordinate(PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER)
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

    private void addPatientSigningDetails(PatientDto patient, Date signedOnDateTime, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException {
        String patientName = UserInfoHelper.getFullName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
        String email = patient.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        final String signedByLabel = "Signed by: ";
        final String emailLabel = "Email: ";
        final String signedOnLabel = "Signed on: ";
        generateConsentSigningDetails(signedByLabel, patientName, emailLabel, email,
                signedOnLabel, PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN),
                startYCoordinate, defaultFont, contentStream);
    }

    private void addNonPatientSigningDetails(String role, Optional<UserDto> signedByUserDto, Date signedOnDateTime, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException {
        UserDto signedUser = signedByUserDto.orElseThrow(NoDataFoundException::new);
        String userFullName = UserInfoHelper.getUserFullName(signedUser);
        String email = signedUser.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        final String signedByLabel = "Signed by ".concat(role.substring(0, 1).toUpperCase() + role.substring(1) + ": ");
        final String emailLabel = "Email: ";
        final String signedOnLabel = "Signed on: ";

        generateConsentSigningDetails(signedByLabel, userFullName, emailLabel, email,
                signedOnLabel, PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN),
                startYCoordinate, defaultFont, contentStream);

        // Add signature details
        final Color textColor = Color.BLACK;
        final float fontSize = PdfBoxStyle.TEXT_SMALL_SIZE;
        final float xCoordinate = 300f;
        String title = "Patient/Patient Representative:";
        pdfBoxService.addTextAtOffset(title, PDType1Font.TIMES_BOLD, fontSize, textColor, xCoordinate, startYCoordinate, contentStream);

        String signatureLabel = "Signature: __________________________";
        final float signatureLabelYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        pdfBoxService.addTextAtOffset(signatureLabel, defaultFont, fontSize, textColor, xCoordinate, signatureLabelYCoordinate, contentStream);

        String printNameLabel = "Print Name: _________________________";
        final float printNameLabelYCoordinate = signatureLabelYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        pdfBoxService.addTextAtOffset(printNameLabel, defaultFont, fontSize, textColor, xCoordinate, printNameLabelYCoordinate, contentStream);

        String dateLabel = "Date: _______________________________";
        final float dateLabelYCoordinate = printNameLabelYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        pdfBoxService.addTextAtOffset(dateLabel, defaultFont, fontSize, textColor, xCoordinate, dateLabelYCoordinate, contentStream);
    }

    private void generateConsentSigningDetails(String signedByLabel, String signedBy, String emailLabel, String email, String signedOnLabel, String signedOn, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException {
        final PDFont contentFont = PDType1Font.TIMES_BOLD;
        final Color textColor = Color.BLACK;
        final float fontSize = PdfBoxStyle.TEXT_SMALL_SIZE;

        // Add Signed by
        pdfBoxService.addTextAtOffset(signedByLabel, defaultFont, fontSize, textColor, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, startYCoordinate, contentStream);
        final float crnXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + PdfBoxHandler.targetedStringWidth(signedByLabel, defaultFont, fontSize);
        pdfBoxService.addTextAtOffset(signedBy, contentFont, fontSize, textColor, crnXCoordinate, startYCoordinate, contentStream);

        // Add Email
        final float emailYCoordinate = startYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        pdfBoxService.addTextAtOffset(emailLabel, defaultFont, fontSize, textColor, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, emailYCoordinate, contentStream);
        final float nameXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + PdfBoxHandler.targetedStringWidth(emailLabel, defaultFont, fontSize);
        pdfBoxService.addTextAtOffset(email, contentFont, fontSize, textColor, nameXCoordinate, emailYCoordinate, contentStream);

        // Add Signed on
        final float signedOnYCoordinate = emailYCoordinate - PdfBoxStyle.XLARGE_LINE_SPACE;
        pdfBoxService.addTextAtOffset(signedOnLabel, defaultFont, fontSize, textColor, PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER, signedOnYCoordinate, contentStream);
        final float dobXCoordinate = PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER + PdfBoxHandler.targetedStringWidth(signedOnLabel, defaultFont, fontSize);
        pdfBoxService.addTextAtOffset(signedOn, contentFont, fontSize, textColor, dobXCoordinate, signedOnYCoordinate, contentStream);
    }

    private void drawSectionHeader(String title, float cardXCoordinate, float cardYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException {
        // Set background color
        Color color = new Color(73, 89, 105);
        float colorBoxWidth = page.getMediaBox().getWidth() - 2 * PdfBoxStyle.LEFT_RIGHT_MARGINS_OF_LETTER;
        float colorBoxHeight = PdfBoxStyle.DEFAULT_TABLE_ROW_HEIGHT;
        PDFont titleFont = PDType1Font.TIMES_BOLD;
        float titleFontSize = PdfBoxStyle.TEXT_MEDIUM_SIZE;
        Color titleColor = Color.WHITE;

        pdfBoxService.addColorBox(color, cardXCoordinate, cardYCoordinate, colorBoxWidth, colorBoxHeight, page, contentStream);

        float titleYCoordinate = cardYCoordinate + (colorBoxHeight / 2)
                - ((titleFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * titleFontSize) / 4);

        pdfBoxService.addTextAtOffset(title, titleFont, titleFontSize, titleColor, cardXCoordinate + 4f, titleYCoordinate, contentStream);
    }

    private String determineProviderName(FlattenedSmallProviderDto providerDto) {
        if (providerDto.getEntityTypeDisplayName().equalsIgnoreCase(PlsService.ProviderType.INDIVIDUAL)) {
            return UserInfoHelper.getFullName(providerDto.getFirstName(), providerDto.getMiddleName(), providerDto.getLastName());
        } else {
            return providerDto.getOrganizationName();
        }
    }

    private String composeAddress(Address address) {
        return address.getLine1()
                .concat(filterNullAddressValue(address.getLine2()))
                .concat(filterNullAddressValue(address.getCity()))
                .concat(filterNullAddressValue(address.getState()))
                .concat(filterNullAddressValue(address.getPostalCode()))
                .concat(filterNullAddressValue(address.getCountry()));
    }

    private String composeAddress(FlattenedSmallProviderDto flattenedSmallProviderDto) {
        return flattenedSmallProviderDto.getFirstLinePracticeLocationAddress()
                .concat(filterNullAddressValue(flattenedSmallProviderDto.getSecondLinePracticeLocationAddress()))
                .concat(filterNullAddressValue(flattenedSmallProviderDto.getPracticeLocationAddressCityName()))
                .concat(filterNullAddressValue(flattenedSmallProviderDto.getPracticeLocationAddressStateName()))
                .concat(filterNullAddressValue(flattenedSmallProviderDto.getPracticeLocationAddressPostalCode()))
                .concat(filterNullAddressValue(flattenedSmallProviderDto.getPracticeLocationAddressCountryCode()));
    }

    private static String filterNullAddressValue(String value) {
        final String commaPattern = ", ";
        if (value == null) {
            return "";
        } else {
            return commaPattern.concat(value);
        }
    }
}
