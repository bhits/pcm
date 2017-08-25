package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableAttribute {
    private float leftMargin;
    private float topMargin;
    private float rowHeight;
    private float cellMargin;
    private PDRectangle pageSize;
    private PDFont contentFont;
    private float contentFontSize;
    private Color borderColor;
    private List<Column> columns;
}
