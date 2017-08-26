package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.geom.Point2D;
import java.io.IOException;

public interface PdfBoxService {
    PDPage generatePage(String typeOfPdf, PDDocument generatedPdDocument);

    void addTextOffsetOriginPoint(String text, PDFont font, int fontSize, Point2D.Float offset,
                         PDPage page, PDPageContentStream contentStream) throws IOException;

    void addCenteredText(String text, PDFont font, int fontSize, Point2D.Float offset,
                         PDPage page, PDPageContentStream contentStream) throws IOException;

    void addTableContent(PDPage page, PDPageContentStream contentStream, TableAttribute tableAttribute,
                         String[][] content) throws IOException;

    byte[] convertPDDocumentToByteArray(PDDocument generatedPdDocument) throws IOException;

    PDFont getConfiguredPdfFont(String typeOfPdf);

    PDRectangle getConfiguredPdfPageSize(String typeOfPdf);
}
