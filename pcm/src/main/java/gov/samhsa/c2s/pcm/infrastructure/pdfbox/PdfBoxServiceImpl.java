package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import gov.samhsa.c2s.pcm.config.PdfProperties;
import gov.samhsa.c2s.pcm.infrastructure.pdfbox.util.PDFontHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class PdfBoxServiceImpl implements PdfBoxService {

    @Autowired
    private PdfProperties pdfProperties;

    @Override
    public PDPage generatePage(String typeOfPdf, PDDocument generatedPdDocument) {
        PDPage page = new PDPage();
        page.setMediaBox(getConfiguredPdfPageSize(typeOfPdf));
        generatedPdDocument.addPage(page);
        return page;
    }

    @Override
    public byte[] convertPDDocumentToByteArray(PDDocument generatedPdDocument) throws IOException {
        PDStream pdStream = new PDStream(generatedPdDocument);
        return pdStream.toByteArray();
    }

    @Override
    public void addTableContent(PDPage page, PDPageContentStream contentStream, TableAttribute tableAttribute, String[][] tableContent) throws IOException {
        //TODO: Verify TableAttribute Valid
        //TODO: Prepare tableContent
        // Draw table line
        drawTableLine(contentStream, tableAttribute, tableContent);

        // Fill the content to table
        fillTextToTable(contentStream, tableAttribute, tableContent);
    }

    /**
     * Get the configured font and will set TIMES ROMAN as default font if it is not configured.
     *
     * @param typeOfPdf
     * @return
     */
    @Override
    public PDFont getConfiguredPdfFont(String typeOfPdf) {
        return pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(typeOfPdf))
                .map(pdfConfig -> PDFontHandler.convertPdfBoxFontToPDFont(pdfConfig.pdFont))
                .findAny()
                .orElse(PDType1Font.TIMES_ROMAN);
    }

    /**
     * Get the configured page size and will set LETTER as default size if it is not configured.
     *
     * @param typeOfPdf
     * @return
     */
    @Override
    public PDRectangle getConfiguredPdfPageSize(String typeOfPdf) {
        return pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(typeOfPdf))
                .map(pdfConfig -> PDFontHandler.convertPdfBoxPageSizeToPDRectangle(pdfConfig.pdfPageSize))
                .findAny()
                .orElse(PDRectangle.LETTER);
    }

    private void drawTableLine(PDPageContentStream contentStream, TableAttribute tableAttribute, String[][] tableContent) throws IOException {
        final int rows = tableContent.length;
        final int cols = tableContent[0].length;
        final float rowHeight = tableAttribute.getRowHeight();

        //set border color
        contentStream.setStrokingColor(tableAttribute.getBorderColor());

        //draw the rows
        final float tableWidth = calculateTableWidth(tableAttribute.getColumns());
        float nextLineY = tableAttribute.getTopMargin();
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(tableAttribute.getLeftMargin(), nextLineY);
            contentStream.lineTo(tableAttribute.getLeftMargin() + tableWidth, nextLineY);
            contentStream.stroke();
            nextLineY -= rowHeight;
        }

        //draw the columns
        final float tableHeight = rowHeight * rows;
        float nextLineX = tableAttribute.getLeftMargin();
        for (int i = 0; i <= cols; i++) {
            contentStream.moveTo(nextLineX, tableAttribute.getTopMargin());
            contentStream.lineTo(nextLineX, tableAttribute.getTopMargin() - tableHeight);
            contentStream.stroke();
            nextLineX += tableAttribute.getColumns().get(i).getCellWidth();
        }
    }

    private void fillTextToTable(PDPageContentStream contentStream, TableAttribute tableAttribute, String[][] tableContent) throws IOException {
        //Set text font and font size
        contentStream.setFont(tableAttribute.getContentFont(), tableAttribute.getContentFontSize());

        final float cellMargin = tableAttribute.getCellMargin();
        // Define to start drawing content at horizontal position
        float nextTextX = tableAttribute.getLeftMargin() + cellMargin;
        // Define to start drawing content at vertical position
        float nextTextY = calculateDrawPositionInVertical(tableAttribute);

        for (String[] aContent : tableContent) {
            int index = 0;
            for (String text : aContent) {
                contentStream.beginText();
                contentStream.newLineAtOffset(nextTextX, nextTextY);
                contentStream.showText(text != null ? text : "");
                contentStream.endText();
                nextTextX += tableAttribute.getColumns().get(index).getCellWidth();
                index++;
            }
            // Update new position cursor after writing the content for one row
            nextTextY -= tableAttribute.getRowHeight();
            nextTextX = tableAttribute.getLeftMargin() + cellMargin;
        }
    }

    private float calculateTableWidth(List<Column> columns) {
        final float initTableWidth = 0f;
        Double tableWidth = columns.stream()
                .mapToDouble(column -> column.getCellWidth() + initTableWidth)
                .sum();
        return tableWidth.floatValue();
    }

    private float calculateDrawPositionInVertical(TableAttribute tableAttribute) {
        return tableAttribute.getTopMargin() - (tableAttribute.getRowHeight() / 2)
                - ((tableAttribute.getContentFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * tableAttribute.getContentFontSize()) / 4);
    }
}
