package gov.samhsa.c2s.pcm.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetCategoryDto {

    /**
     * The code.
     */
    @NotBlank
    private String code;

    /**
     * The display name.
     */
    private String displayName;

    /**
     * The description.
     */
    private String description;

    /**
     * The isFederal.
     */
    private boolean isFederal;

    /**
     * The displayOrder.
     */
    private int displayOrder;

    /**
     * The system.
     */
    @NotBlank
    private String system;
}