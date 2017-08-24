package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

public interface PdfBoxService {
    byte[] convertPDDocumentToByteArray(PDDocument generatedPdDocument) throws IOException;

    void createTableContent(PDPage page, PDPageContentStream contentStream, String[][] content,
                            TableAttribute tableAttribute) throws IOException;

    PDFont getConfiguredPdfFont(String typeOfPdf);
}
