package gov.samhsa.c2s.pcm.infrastructure.pdf;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import gov.samhsa.c2s.pcm.domain.Consent;

import java.time.LocalDate;
import java.util.Date;

public interface ITextPdfService {
    //Create document title
    Paragraph createParagraphWithContent(String title, Font font);

    // Create borderless table
    PdfPTable createBorderlessTable(int column);

    PdfPCell createBorderlessCell(String content, Font font);

    Chunk createChunkWithFont(String text, Font textFont);

    Paragraph createCellContent(String label, Font labelFont, String value, Font valueFont);

    PdfPTable createSectionTitle(String title);

    Chunk createUnderlineText(String text);

    PdfPCell createEmptyBorderlessCell();

    PdfPCell createCellWithUnderlineContent(String text);

    List createUnorderList(java.util.List<String> items);

    PdfPTable createConsentReferenceNumberTable(Consent consent);

    PdfPTable createPatientSigningDetailsTable(String firstName, String lastName, String email, boolean isSigned, Date attestedOn);

    PdfPTable createProviderSigningDetailsTable(String firstName, String lastName, String email, boolean isSigned, Date attestedOn);

    PdfPTable createPatientNameAndDOBTable(String firstName, String lastName, Date birthDate);

    String formatDate(Date aDate);

    public String formatLocalDate(LocalDate aDate);

    String getFullName(String firstName, String lastName);
}
