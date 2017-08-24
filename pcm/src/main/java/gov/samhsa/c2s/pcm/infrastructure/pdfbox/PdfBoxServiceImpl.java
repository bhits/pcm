package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PDFontHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class PdfBoxServiceImpl implements PdfBoxService {

    @Autowired
    private PdfProperties pdfProperties;

    @Override
    public byte[] convertPDDocumentToByteArray(PDDocument generatedPdDocument) throws IOException {
        PDStream pdStream = new PDStream(generatedPdDocument);
        return pdStream.toByteArray();
    }

    @Override
    public void createTableContent(PDPage page, PDPageContentStream contentStream, String[][] tableContent, TableAttribute tableAttribute) throws IOException {
        final int rows = tableContent.length;
        final int cols = tableContent[0].length;
        final float rowHeight = tableAttribute.getRowHeight();
        final float cellMargin = tableAttribute.getCellMargin();

        final float tableWidth = page.getMediaBox().getWidth() - (2 * tableAttribute.getLeftRightPadding());
        final float tableHeight = rowHeight * rows;
        final float colWidth = tableWidth / (float) cols;

        //draw the rows
        float nexty = tableAttribute.getTopPadding();
        for (int i = 0; i <= rows; i++) {
            contentStream.drawLine(tableAttribute.getLeftRightPadding(), nexty, tableAttribute.getLeftRightPadding() + tableWidth, nexty);
            nexty -= rowHeight;
        }

        //draw the columns
        float nextx = tableAttribute.getLeftRightPadding();
        for (int i = 0; i <= cols; i++) {
            contentStream.drawLine(nextx, tableAttribute.getTopPadding(), nextx, tableAttribute.getTopPadding() - tableHeight);
            nextx += colWidth;
        }

        //now add the text
        contentStream.setFont(tableAttribute.getTableContentFont(), tableAttribute.getTableContentFontSize());

        float textx = tableAttribute.getLeftRightPadding() + cellMargin;
        float texty = tableAttribute.getTopPadding() - 15;
        for (String[] aContent : tableContent) {
            for (String text : aContent) {
                contentStream.beginText();
                contentStream.newLineAtOffset(textx, texty);
                contentStream.showText(text);
                contentStream.endText();
                textx += colWidth;
            }
            texty -= rowHeight;
            textx = tableAttribute.getLeftRightPadding() + cellMargin;
        }
    }

    @Override
    public PDFont getConfiguredPdfFont(String typeOfPdf) {
        return pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(typeOfPdf))
                .map(pdfConfig -> PDFontHandler.convertPdfBoxFontToPDFont(pdfConfig.pdFont))
                .findAny()
                .orElse(PDType1Font.TIMES_ROMAN);
    }
}
