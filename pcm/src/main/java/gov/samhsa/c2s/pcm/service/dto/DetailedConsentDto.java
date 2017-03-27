package gov.samhsa.c2s.pcm.service.dto;

import gov.samhsa.c2s.common.validator.constraint.PresentOrFuture;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ScriptAssert(
        lang = "javascript",
        alias = "_",
        script = "_.startDate != null && _.endDate != null && _.startDate.isBefore(_.endDate)",
        message = "consent end date must be after consent start date")
public class DetailedConsentDto {

    private Long id;

    @Valid
    @NotNull
    private List<AbstractProviderDto> fromProviders = new ArrayList<>();

    @Valid
    @NotNull
    private List<AbstractProviderDto> toProviders = new ArrayList<>();

    @Valid
    @NotNull
    private List<SensitivityCategoryDto> shareSensitivityCategories;

    @Valid
    @NotNull
    private List<PurposeDto> sharePurposes;

    @NotNull
    @PresentOrFuture
    private LocalDate startDate;

    @NotNull
    @Future
    private LocalDate endDate;

    @NotNull
    private ConsentStage consentStage;
}
