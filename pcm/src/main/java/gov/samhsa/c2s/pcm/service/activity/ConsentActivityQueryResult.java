package gov.samhsa.c2s.pcm.service.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentActivityQueryResult {
    private String consentReferenceId;
    private String consentStage;
    private String revType;
    private String lastUpdatedBy;
    private String lastUpdatedDateTime;
}
