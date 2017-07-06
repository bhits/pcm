package gov.samhsa.c2s.pcm.infrastructure.pdf;


import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.SensitivityCategory;
import gov.samhsa.c2s.pcm.domain.valueobject.Address;
import gov.samhsa.c2s.pcm.infrastructure.PlsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.FlattenedSmallProviderDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ConsentPdfGeneratorImpl implements ConsentPdfGenerator {


    private final ITextPdfService iTextPdfService;

    private final PlsService plsService;

    private final String EMAIL = "EMAIL";

    private static final String CREATE_CONSENT_TITLE = "Consent to Share My Health Information";

    @Autowired
    public ConsentPdfGeneratorImpl(ITextPdfService iTextPdfService, PlsService plsService) {
        this.iTextPdfService = iTextPdfService;
        this.plsService = plsService;
    }


    @Override
    public byte[] generate42CfrPart2Pdf(Consent consent, PatientDto patientProfile, boolean isSigned, Date attestedOn, String consentTerms, Optional<UserDto> attesterUserDto) {
        Assert.notNull(consent, "Consent is required.");

        Document document = new Document();

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, pdfOutputStream);

            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
            document.add(iTextPdfService.createParagraphWithContent(CREATE_CONSENT_TITLE, titleFont));

            // Blank line
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(" "));

            //consent Reference Number
            document.add(iTextPdfService.createConsentReferenceNumberTable(consent));

            //Patient Name and date of birth
            document.add(iTextPdfService.createPatientNameAndDOBTable(patientProfile.getFirstName(), patientProfile.getLastName(), java.sql.Date.valueOf(patientProfile.getBirthDate())));

            document.add(new Paragraph(" "));

            //Authorization to disclose section
            document.add(iTextPdfService.createSectionTitle(" AUTHORIZATION TO DISCLOSE"));
            //Authorizes
            document.add(new Paragraph("Authorizes: "));

            document.add(createProviderPermittedToDiscloseTable(consent));
            //To disclose to
            document.add(new Paragraph("To disclose to: "));

            document.add(createProviderDisclosureIsMadeToTable(consent));

            document.add(new Paragraph(" "));

            //Health information to be disclosed section
            document.add(iTextPdfService.createSectionTitle(" HEALTH INFORMATION TO BE DISCLOSED"));

            document.add(createHealthInformationToBeDisclose(consent));

            document.add(new Paragraph(" "));

            //Consent terms section
            document.add(iTextPdfService.createSectionTitle(" CONSENT TERMS"));

            // Consent term
            document.add(createConsentTerms(consentTerms, patientProfile));

            document.add(new Paragraph(" "));

            // Consent effective and expiration date
            document.add(createStartAndEndDateTable(consent));

            document.add(new Paragraph(" "));
            String email = patientProfile.getTelecoms().stream().filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(EMAIL)).findFirst().get().getValue();

            //Signing details
            if(attesterUserDto.isPresent()){
                String firstName = attesterUserDto.get().getFirstName();
                String lastName = attesterUserDto.get().getLastName();
                email = attesterUserDto.get().getTelecoms().stream().filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(EMAIL)).findFirst().get().getValue();
                document.add(iTextPdfService.createProviderSigningDetailsTable(firstName, lastName, email, isSigned, attestedOn));
                document.add(iTextPdfService.createSpaceForSignatureByPatientAndOtherRole("Provider"));
            } else {
                document.add(iTextPdfService.createPatientSigningDetailsTable(patientProfile.getFirstName(), patientProfile.getLastName(), email, isSigned, attestedOn));
            }



            document.close();

        } catch (Throwable e) {
            //TODO: throw exception
        }

        return pdfOutputStream.toByteArray();
    }

    private String getFullName(PatientDto patientProfile) {
        return patientProfile.getFirstName() + " " + patientProfile.getLastName();
    }

    private PdfPTable createStartAndEndDateTable(Consent consent) {
        PdfPTable consentStartAndEndDateTable = iTextPdfService.createBorderlessTable(3);

        if (consent != null) {
            Font patientDateFont = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.BOLD);
            PdfPCell EffectiveDateCell = new PdfPCell(iTextPdfService.createCellContent("Effective Date: ", patientDateFont, iTextPdfService.formatLocalDate(consent.getStartDate().toLocalDate()), patientDateFont));
            EffectiveDateCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell expirationDateCell = new PdfPCell(iTextPdfService.createCellContent("Expiration Date: ", patientDateFont, iTextPdfService.formatLocalDate(consent.getEndDate().toLocalDate()), patientDateFont));
            expirationDateCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(Rectangle.NO_BORDER);

            consentStartAndEndDateTable.addCell(EffectiveDateCell);
            consentStartAndEndDateTable.addCell(expirationDateCell);
            consentStartAndEndDateTable.addCell(emptyCell);
        }
        return consentStartAndEndDateTable;
    }

    private Paragraph createConsentTerms(String terms, PatientDto patientProfile) {
        String userNameKey = "ATTESTER_FULL_NAME";
        String termsWithAttestedName = StrSubstitutor.replace(terms, ImmutableMap.of("ATTESTER_FULL_NAME", getFullName(patientProfile)));
        return iTextPdfService.createParagraphWithContent(termsWithAttestedName, null);
    }

    private PdfPTable createProviderPropertyValueTable(String propertyName, String propertyValue) {
        PdfPTable providerTable = iTextPdfService.createBorderlessTable(1);

        Font propertyNameFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);
        providerTable.addCell(iTextPdfService.createBorderlessCell(propertyName, propertyNameFont));
        Font valueFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        providerTable.addCell(iTextPdfService.createBorderlessCell(propertyValue, valueFont));

        return providerTable;
    }

    private String composeAddress(Address address) {
        StringBuilder addressString = new StringBuilder();
        if (address.getLine1() != null) {
            addressString.append(address.getLine1()).append(", ");
        }

        if (address.getLine2() != null) {
            addressString.append(address.getLine2()).append(", ");
        }

        if (address.getCity() != null) {
            addressString.append(address.getCity()).append(", ");
        }


        if (address.getState() != null && address.getPostalCode() != null) {
            addressString.append(address.getState()).append(", ").append(address.getPostalCode());
        }

        return addressString.toString();
    }

    private String composeAddress(FlattenedSmallProviderDto providerDto) {
        StringBuilder addressString = new StringBuilder();
        if (providerDto.getFirstLinePracticeLocationAddress() != null) {
            addressString.append(providerDto.getFirstLinePracticeLocationAddress()).append(", ");
        }

        if (providerDto.getSecondLinePracticeLocationAddress() != null) {
            addressString.append(providerDto.getSecondLinePracticeLocationAddress()).append(", ");
        }

        if (providerDto.getPracticeLocationAddressCityName() != null) {
            addressString.append(providerDto.getPracticeLocationAddressCityName()).append(", ");
        }


        if (providerDto.getPracticeLocationAddressStateName() != null && providerDto.getPracticeLocationAddressPostalCode() != null) {
            addressString.append(providerDto.getPracticeLocationAddressStateName()).append(", ").append(providerDto.getPracticeLocationAddressPostalCode());
        }

        return addressString.toString();
    }

    private PdfPTable createProviderPermittedToDiscloseTable(Consent consent) {
        PdfPTable providerTable = iTextPdfService.createBorderlessTable(4);
        if (consent.getConsentAttestation() != null) {
            consent.getConsentAttestation().getFromOrganizations().stream().
                    forEach(organization -> {
                        providerTable.addCell(createProviderPropertyValueTable("Provider Name", organization.getName()));
                        providerTable.addCell(createProviderPropertyValueTable("NPI Number", organization.getProvider().getIdentifier().getValue()));
                        providerTable.addCell(createProviderPropertyValueTable("Address", composeAddress(organization.getAddress())));
                        providerTable.addCell(createProviderPropertyValueTable("Phone", organization.getPhoneNumber()));

                    });

            consent.getConsentAttestation().getFromPractitioners().stream().
                    forEach(practitioner -> {
                        providerTable.addCell(createProviderPropertyValueTable("Provider Name", practitioner.getFirstName() + " " + practitioner.getLastName()));
                        providerTable.addCell(createProviderPropertyValueTable("NPI Number", practitioner.getProvider().getIdentifier().getValue()));
                        providerTable.addCell(createProviderPropertyValueTable("Address", composeAddress(practitioner.getAddress())));
                        providerTable.addCell(createProviderPropertyValueTable("Phone", practitioner.getPhoneNumber()));

                    });
        } else {
            consent.getFromProviders().stream().map(provider -> plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER))
                    .forEach(providerDto -> {
                        if (providerDto.getEntityTypeDisplayName().equals(PlsService.ProviderType.INDIVIDUAL))
                            providerTable.addCell(createProviderPropertyValueTable("Provider Name", providerDto.getFirstName() + " " + providerDto.getLastName()));
                        else
                            providerTable.addCell(createProviderPropertyValueTable("Provider Name", providerDto.getOrganizationName()));
                        providerTable.addCell(createProviderPropertyValueTable("NPI Number", providerDto.getNpi()));
                        providerTable.addCell(createProviderPropertyValueTable("Address", composeAddress(providerDto)));
                        providerTable.addCell(createProviderPropertyValueTable("Phone", providerDto.getPracticeLocationAddressTelephoneNumber()));

                    });

        }

        return providerTable;
    }

    private PdfPTable createProviderDisclosureIsMadeToTable(Consent consent) {
        PdfPTable providerTable = iTextPdfService.createBorderlessTable(4);
        if (consent.getConsentAttestation() != null) {
            consent.getConsentAttestation().getToOrganizations().stream().
                    forEach(organization -> {
                        providerTable.addCell(createProviderPropertyValueTable("Provider Name", organization.getName()));
                        providerTable.addCell(createProviderPropertyValueTable("NPI Number", organization.getProvider().getIdentifier().getValue()));
                        providerTable.addCell(createProviderPropertyValueTable("Address", composeAddress(organization.getAddress())));
                        providerTable.addCell(createProviderPropertyValueTable("Phone", organization.getPhoneNumber()));

                    });

            consent.getConsentAttestation().getToPractitioners().stream().
                    forEach(practitioner -> {
                        providerTable.addCell(createProviderPropertyValueTable("Provider Name", practitioner.getFirstName() + " " + practitioner.getLastName()));
                        providerTable.addCell(createProviderPropertyValueTable("NPI Number", practitioner.getProvider().getIdentifier().getValue()));
                        providerTable.addCell(createProviderPropertyValueTable("Address", composeAddress(practitioner.getAddress())));
                        providerTable.addCell(createProviderPropertyValueTable("Phone", practitioner.getPhoneNumber()));
                    });
        } else {
            consent.getFromProviders().stream().map(provider -> plsService.getFlattenedSmallProvider(provider.getIdentifier().getValue(), PlsService.Projection.FLATTEN_SMALL_PROVIDER))
                    .forEach(providerDto -> {
                        if (providerDto.getEntityTypeDisplayName().equals(PlsService.ProviderType.INDIVIDUAL))
                            providerTable.addCell(createProviderPropertyValueTable("Provider Name", providerDto.getFirstName() + " " + providerDto.getLastName()));
                        else
                            providerTable.addCell(createProviderPropertyValueTable("Provider Name", providerDto.getOrganizationName()));
                        providerTable.addCell(createProviderPropertyValueTable("NPI Number", providerDto.getNpi()));
                        providerTable.addCell(createProviderPropertyValueTable("Address", composeAddress(providerDto)));
                        providerTable.addCell(createProviderPropertyValueTable("Phone", providerDto.getPracticeLocationAddressTelephoneNumber()));

                    });

        }
        return providerTable;
    }


    private PdfPTable createHealthInformationToBeDisclose(Consent consent) {
        PdfPTable healthInformationToBeDisclose = iTextPdfService.createBorderlessTable(2);

        // Medical Information
        PdfPCell medicalInformation = iTextPdfService.createCellWithUnderlineContent("To SHARE the following medical information:");

        Paragraph sensitivityCategoryParagraph = iTextPdfService.createParagraphWithContent("Sensitivity Categories:", null);

        medicalInformation.addElement(sensitivityCategoryParagraph);

        List<String> sensitivityCategoryList = consent.getShareSensitivityCategories().stream()
                .map(SensitivityCategory::getDisplay).collect(toList());

        medicalInformation.addElement(iTextPdfService.createUnorderList(sensitivityCategoryList));
        healthInformationToBeDisclose.addCell(medicalInformation);

        //Purposes of use
        PdfPCell purposeOfUseCell = iTextPdfService.createCellWithUnderlineContent("To SHARE for the following purpose(s):");
        List<String> purposes = getPurposeOfUse(consent);
        purposeOfUseCell.addElement(iTextPdfService.createUnorderList(purposes));
        healthInformationToBeDisclose.addCell(purposeOfUseCell);

        return healthInformationToBeDisclose;
    }

    private List<String> getMedicalInformation(Consent consent) {
        return consent.getShareSensitivityCategories().stream().map(SensitivityCategory::getDisplay).collect(toList());
    }

    private List<String> getPurposeOfUse(Consent consent) {
        return consent.getSharePurposes().stream().map(Purpose::getDisplay).collect(toList());
    }

}
