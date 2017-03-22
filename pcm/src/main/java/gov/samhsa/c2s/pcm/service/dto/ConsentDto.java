package gov.samhsa.c2s.pcm.service.dto;

import gov.samhsa.c2s.common.validator.constraint.PresentOrFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ScriptAssert(
        lang = "javascript",
        alias = "_",
        script = "_.startDate != null && _.endDate != null && _.startDate < _.endDate",
        message = "consent end date must be after consent start date")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentDto {
    @Valid
    @NotNull
    private IdentifiersDto fromProviders;

    @Valid
    @NotNull
    private IdentifiersDto toProviders;

    @Valid
    @NotNull
    private IdentifiersDto shareSensitivityCategories;

    @Valid
    @NotNull
    private IdentifiersDto sharePurposes;

    @NotNull
    @PresentOrFuture
    private LocalDate startDate;

    @NotNull
    @Future
    private LocalDate endDate;
}
