package gov.samhsa.c2s.pcm.infrastructure.pdfbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.font.PDFont;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableAttribute {
    private float rowHeight;
    private float cellMargin;
    private float leftRightPadding;
    private float topPadding;
    private PDFont tableContentFont;
    private int tableContentFontSize;
}
