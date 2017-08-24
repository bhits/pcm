package gov.samhsa.c2s.pcm.infrastructure.pdfbox.util;

import gov.samhsa.c2s.pcm.infrastructure.pdfbox.PdfBoxFont;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PDFontHandler {

    public static PDFont convertPdfBoxFontToPDFont(PdfBoxFont configuredFont) {
        return buildPDFontMap().get(configuredFont);
    }

    private static Map<PdfBoxFont, PDFont> buildPDFontMap() {
        return Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>(PdfBoxFont.TIMES_ROMAN, PDType1Font.TIMES_ROMAN),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.TIMES_BOLD, PDType1Font.TIMES_BOLD),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.TIMES_ITALIC, PDType1Font.TIMES_ITALIC),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.TIMES_BOLD_ITALIC, PDType1Font.TIMES_BOLD_ITALIC),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.HELVETICA, PDType1Font.HELVETICA),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.HELVETICA_BOLD, PDType1Font.HELVETICA_BOLD),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.HELVETICA_OBLIQUE, PDType1Font.HELVETICA_OBLIQUE),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.HELVETICA_BOLD_OBLIQUE, PDType1Font.HELVETICA_BOLD_OBLIQUE),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.COURIER, PDType1Font.COURIER),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.COURIER_BOLD, PDType1Font.COURIER_BOLD),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.COURIER_OBLIQUE, PDType1Font.COURIER_OBLIQUE),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.COURIER_BOLD_OBLIQUE, PDType1Font.COURIER_BOLD_OBLIQUE),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.SYMBOL, PDType1Font.SYMBOL),
                new AbstractMap.SimpleEntry<>(PdfBoxFont.ZAPF_DINGBATS, PDType1Font.ZAPF_DINGBATS))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }
}
