package gov.samhsa.c2s.pcm.service.pdf;


import com.google.common.collect.ImmutableMap;
import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.domain.Provider;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.SensitivityCategory;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.TelecomDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PdfBoxHandler;
import gov.samhsa.c2s.pcm.service.exception.NoDataFoundException;
import gov.samhsa.c2s.pcm.service.exception.PdfConfigMissingException;
import gov.samhsa.c2s.pcm.service.pdf.hexPdf.Footer;
import gov.samhsa.c2s.pcm.service.pdf.hexPdf.HexPDF;
import gov.samhsa.c2s.pcm.service.util.UserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary // TODO remove when finished with the refactoring
public class ConsentPdfGeneratorWithHexPdfImpl implements ConsentPdfGenerator {
    private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy";
    private static final String CONSENT_PDF = "consent-pdf";
    private static final String TELECOM_EMAIL = "EMAIL";

    private final PdfProperties pdfProperties;
    private final PlsService plsService;
    private HexPDF document;

    @Autowired
    public ConsentPdfGeneratorWithHexPdfImpl(PdfProperties pdfProperties, PlsService plsService) {
        this.pdfProperties = pdfProperties;
        this.plsService = plsService;
    }

    @Override
    public byte[] generateConsentPdf(Consent consent, PatientDto patientProfile, Date operatedOnDateTime, String consentTerms, Optional<UserDto> operatedByUserDto, Optional<Boolean> operatedByPatient) throws IOException {
        Assert.notNull(consent, "Consent is required.");

        String consentTitle = getConsentTitle(CONSENT_PDF);

        document = new HexPDF();

        setPageFooter(document, consentTitle);

        // Create the first page
        document.newPage();

        // Set document title
        drawConsentTitle(document, consentTitle);

        // Typeset everything else in boring black
        document.setTextColor(Color.black);

        document.normalStyle();

        drawPatientInformationSection(document, consent, patientProfile);

        drawAuthorizeToDiscloseSectionTitle(consent);

        drawHealthInformationToBeDisclosedSection(consent);

        drawConsentTermsSection(consentTerms, patientProfile);

        drawEffectiveAndExspireDateSection(consent);

//       Consent signing details
        if (consent.getConsentStage().equals(ConsentStage.SIGNED)) {
            addConsentSigningDetails(document, patientProfile, operatedByUserDto, operatedOnDateTime, operatedByPatient);
        }
        // Get the document
        return document.getDocumentAsBytArray();
    }

    @Override
    public String getConsentTitle(String pdfType){
        return  pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(pdfType))
                .map(PdfProperties.PdfConfig::getTitle)
                .findAny()
                .orElseThrow(PdfConfigMissingException::new);
    }


    public void drawConsentTitle(HexPDF document, String consentTitle){
        // Add a main title, centered in shiny colours
        document.title1Style();
        document.drawText( consentTitle + "\n", HexPDF.CENTER);
    }
    @Override
    public void setPageFooter(HexPDF document, String consentTitle){
        document.setFooter(Footer.defaultFooter);
        // Change center text in footer
        document.getFooter().setCenterText(consentTitle);
        // Use footer also on first page
        document.getFooter().setOMIT_FIRSTPAGE(false);
    }
    @Override
    public void drawPatientInformationSection(HexPDF document, Consent consent, PatientDto patientProfile){
        String patientFullName = UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName());
        String patientBirthDate = PdfBoxHandler.formatLocalDate(patientProfile.getBirthDate(), DATE_FORMAT_PATTERN);

        Object[][] patientInfo = {
                {"Consent Reference Number: " + consent.getConsentReferenceId() , null},
                {"Patient Name: " + patientFullName, "Patient DOB: "+ patientBirthDate}
        };

        document.drawTable(patientInfo,
                new float[]{240,240},
                new int[]{HexPDF.LEFT, HexPDF.LEFT},
                HexPDF.LEFT);

    }

    private void drawAuthorizeToDiscloseSectionTitle(Consent consent){
        Object[][] title = {
                {"AUTHORIZATION TO DISCLOSE" }
        };
        document.drawTable(title,
                new float[]{480},
                new int[]{HexPDF.LEFT},
                HexPDF.LEFT);
        drawAuthorizationSubSectionHeader(document,"\nAuthorizes:\n" );

        drawProvidersTable(document, consent.getFromProviders());

        drawAuthorizationSubSectionHeader(document,"\nTo disclose to:\n" );

        drawProvidersTable(document, consent.getToProviders());
    }

    private void drawHealthInformationToBeDisclosedSection(Consent consent) {
        document.drawText(  "\n");

        Object[][] title = {
                {"HEALTH INFORMATION TO BE DISCLOSED" }
        };
        document.drawTable(title,
                new float[]{480},
                new int[]{HexPDF.LEFT},
                HexPDF.LEFT);

        String sensitivityCategoriesLabel = "To SHARE the following medical information:";
        String subLabel = "Sensitivity Categories:";
        String listPrifix = "\n- ";
        String sensitivityCategories = consent.getShareSensitivityCategories().stream()
                .map(SensitivityCategory::getDisplay)
                .collect(Collectors.joining("\n- "));

        String sensitivityCategoriesStr = sensitivityCategoriesLabel
                                            .concat("\n").concat(subLabel)
                                            .concat("\n- ").concat(sensitivityCategories);

        String purposeLabel = "To SHARE for the following purpose(s):";

        String purposes = consent.getSharePurposes().stream()
                .map(Purpose::getDisplay)
                .collect(Collectors.joining("\n- "));
        String purposeOfUseStr = purposeLabel.concat("\n- ").concat(purposes);

        Object[][] healthInformationHeaders = {
                {sensitivityCategoriesStr, purposeOfUseStr }
        };

        document.drawTable(healthInformationHeaders,
                new float[]{240,240},
                new int[]{HexPDF.LEFT, HexPDF.LEFT},
                HexPDF.LEFT);
    }

    private void drawConsentTermsSection(String consentTerms, PatientDto patientProfile) {

        Object[][] title = {
                {"CONSENT TERMS" }
        };

        document.drawTable(title,
                new float[]{480},
                new int[]{HexPDF.LEFT},
                HexPDF.LEFT);

        final String userNameKey = "ATTESTER_FULL_NAME";
        String termsWithAttestedName = StrSubstitutor.replace(consentTerms,
                ImmutableMap.of(userNameKey, UserInfoHelper.getFullName(patientProfile.getFirstName(), patientProfile.getMiddleName(), patientProfile.getLastName())));


        document.drawText(  termsWithAttestedName);
    }


    private void drawEffectiveAndExspireDateSection(Consent consent) {
        // Prepare table content
        String effectiveDateContent = "Effective Date: ".concat(PdfBoxHandler.formatLocalDate(consent.getStartDate().toLocalDate(), DATE_FORMAT_PATTERN));
        String expirationDateContent = "Expiration Date: ".concat(PdfBoxHandler.formatLocalDate(consent.getEndDate().toLocalDate(), DATE_FORMAT_PATTERN));

        Object[][] title = {
                {effectiveDateContent, expirationDateContent }
        };
        document.drawText(  "\n\n");

        document.drawTable(title,
                new float[]{240, 240},
                new int[]{HexPDF.LEFT, HexPDF.LEFT},
                HexPDF.LEFT);
    }

    private void drawAuthorizationSubSectionHeader(HexPDF document, String header){
        document.title2Style();
        document.drawText( header );
        document.normalStyle();
    }

    private void drawProvidersTable(HexPDF document, List<Provider> providers){
        Object[][] tableContents = new Object[providers.size()+1][4];
        tableContents[0][0] =  "Provider Name";
        tableContents[0][1] =  "NPI Number";
        tableContents[0][2] =  "Address";
        tableContents[0][3] =  "Phone" ;

        List<FlattenedSmallProviderDto> flattenedSmallProvidersDto = providers.stream()
                .map(provider -> plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER))
                .collect(Collectors.toList());

        for(int i = 0; i<flattenedSmallProvidersDto.size(); i++){
            tableContents[i+1][0]= determineProviderName(flattenedSmallProvidersDto.get(i));
            tableContents[i+1][1]= flattenedSmallProvidersDto.get(i).getNpi();
            tableContents[i+1][2]= composeAddress(flattenedSmallProvidersDto.get(i)) ;
            tableContents[i+1][3]= flattenedSmallProvidersDto.get(i).getPracticeLocationAddressTelephoneNumber();
        }

        document.drawTable(tableContents,
                new float[]{160,80,160,80},
                new int[]{HexPDF.LEFT, HexPDF.LEFT, HexPDF.LEFT, HexPDF.LEFT},
                HexPDF.LEFT);
    }

    @Override
    public void addConsentSigningDetails(HexPDF document, PatientDto patient, Optional<UserDto> signedByUserDto, Date signedOnDateTime, Optional<Boolean> signedByPatient) throws IOException {
        if (signedByPatient.orElseThrow(NoDataFoundException::new)) {
            // Consent is signed by Patient
            addPatientSigningDetails(document, patient, signedOnDateTime);
        } else {
            // Consent is NOT signed by Patient
            //Todo: Will identify different role once C2S support for multiple role.
            String role = "Provider";
            addNonPatientSigningDetails(role, signedByUserDto, signedOnDateTime);
        }
    }
    private void addPatientSigningDetails(HexPDF document, PatientDto patient, Date signedOnDateTime ) throws IOException {
        Object[][] signedDetails = {
                {createSignatureContent(patient, signedOnDateTime)}
        };

        document.drawTable(signedDetails,
                new float[]{480},
                new int[]{HexPDF.LEFT},
                HexPDF.LEFT);
    }

    private String createSignatureContent(PatientDto patient, Date signedOnDateTime){
        String patientName = UserInfoHelper.getFullName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
        String email = patient.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        final String signedByContent = "\nSigned by: ".concat(patientName);
        final String signedByEmail = "Email: ".concat(email);
        final String signedOn = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN));

        return signedByContent.concat("\n").concat(signedByEmail).concat("\n").concat(signedOn).concat("\n");
    }

    private String createSignatureOnBehalfOfContent(PatientDto patient, Date signedOnDateTime){
        String patientName = UserInfoHelper.getFullName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
        String email = patient.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        final String signedByContent = "Signed by: ".concat(patientName);
        final String signedByEmail = "Email: ".concat(email);
        final String signedOn = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN));

        return signedByContent.concat("\n").concat(signedByEmail).concat("\n").concat(signedOn);
    }

    private void addNonPatientSigningDetails(String role, Optional<UserDto> signedByUserDto, Date signedOnDateTime) throws IOException {
        UserDto signedUser = signedByUserDto.orElseThrow(NoDataFoundException::new);
        String userFullName = UserInfoHelper.getUserFullName(signedUser);
        String email = signedUser.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(TELECOM_EMAIL))
                .findAny()
                .map(TelecomDto::getValue)
                .orElseThrow(NoDataFoundException::new);
        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        final String signedByContent = "Signed by ".concat(role.substring(0, 1).toUpperCase() + role.substring(1) + ": ");
        final String signedByEmail = "Email: ".concat(email);
        final String signedOn = "Signed on: ".concat(PdfBoxHandler.formatLocalDate(signedDate, DATE_FORMAT_PATTERN));
        final String signedContent = signedByContent.concat("\n").concat(signedByEmail).concat("\n").concat(signedOn);

        // Add signature details
        String title = "Patient/Patient Representative:";
        String signatureLabel = "Signature: __________________________";
        String printNameLabel = "Print Name: _________________________";
        String dateLabel = "Date: _______________________________";

        String onBehaveContent = title.concat("\n").concat(signatureLabel).concat("\n").concat(printNameLabel).concat("\n").concat(dateLabel);
        Object[][] signedDetails = {
                {signedContent, onBehaveContent}
        };

        document.drawTable(signedDetails,
                new float[]{240, 240},
                new int[]{HexPDF.LEFT, HexPDF.LEFT},
                HexPDF.LEFT);

    }
    private String determineProviderName(FlattenedSmallProviderDto providerDto) {
        if (providerDto.getEntityTypeDisplayName().equalsIgnoreCase(PlsService.ProviderType.INDIVIDUAL)) {
            return UserInfoHelper.getFullName(providerDto.getFirstName(), providerDto.getMiddleName(), providerDto.getLastName());
        } else {
            return providerDto.getOrganizationName();
        }
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
