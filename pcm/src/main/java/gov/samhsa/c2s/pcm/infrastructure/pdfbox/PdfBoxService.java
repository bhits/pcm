package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

public interface PdfBoxService {
    PDPage generatePage(String typeOfPdf, PDDocument generatedPdDocument);

    byte[] convertPDDocumentToByteArray(PDDocument generatedPdDocument) throws IOException;

    void addTableContent(PDPage page, PDPageContentStream contentStream, TableAttribute tableAttribute,
                         String[][] content) throws IOException;

    PDFont getConfiguredPdfFont(String typeOfPdf);

    PDRectangle getConfiguredPdfPageSize(String typeOfPdf);
}
