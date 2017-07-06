package gov.samhsa.c2s.pcm.infrastructure.pdf;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.exception.ConsentPdfGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Optional;

@Service
public class ConsentRevocationPdfGeneratorImpl implements ConsentRevocationPdfGenerator {


    private final ITextPdfService iTextPdfService;

    private final String EMAIL = "EMAIL";
    private static final String CONSENT_REVOCATION_TITLE = "Revocation of Consent to Share My Health Information";

    @Autowired
    public ConsentRevocationPdfGeneratorImpl(ITextPdfService iTextPdfService) {
        this.iTextPdfService = iTextPdfService;
    }

    @Override
    public byte[] generateConsentRevocationPdf(Consent consent, PatientDto patient, Date attestedOnDateTime, String consentRevocationTerm, Optional<UserDto> revokedByUserDto) {
        Assert.notNull(consent, "Consent is required.");

        Document document = new Document();

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, pdfOutputStream);

            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
            document.add(iTextPdfService.createParagraphWithContent(CONSENT_REVOCATION_TITLE, titleFont));

            // Blank line
            document.add(Chunk.NEWLINE);

            //consent Reference Number
            document.add(iTextPdfService.createConsentReferenceNumberTable(consent));

            document.add(new Paragraph(" "));

            //Patient Name and date of birth
            document.add(iTextPdfService.createPatientNameAndDOBTable(patient.getFirstName(), patient.getLastName(), java.sql.Date.valueOf(patient.getBirthDate())));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(consentRevocationTerm));

            document.add(new Paragraph(" "));
            String email = patient.getTelecoms().stream().filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(EMAIL)).findFirst().get().getValue();

            //Signing details
            if (revokedByUserDto.isPresent()) {
                String firstName = revokedByUserDto.get().getFirstName();
                String lastName = revokedByUserDto.get().getLastName();
                email = revokedByUserDto.get().getTelecoms().stream().filter(telecomDto -> telecomDto.getSystem().equalsIgnoreCase(EMAIL)).findFirst().get().getValue();
                document.add(iTextPdfService.createNonPatientSigningDetailsTable("Provider", firstName, lastName, email, true, attestedOnDateTime));
            } else {
                document.add(iTextPdfService.createPatientSigningDetailsTable(patient.getFirstName(), patient.getLastName(), email, true, attestedOnDateTime));
            }

            document.close();

        } catch (Throwable e) {
            throw new ConsentPdfGenerationException("Exception when trying to generate pdf", e);
        }

        return pdfOutputStream.toByteArray();
    }

}
