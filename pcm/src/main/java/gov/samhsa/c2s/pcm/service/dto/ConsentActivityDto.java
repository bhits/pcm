package gov.samhsa.c2s.pcm.service.dto;

import gov.samhsa.c2s.pcm.infrastructure.dto.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentActivityDto {
    private String consentReferenceId;
    private String actionType;
    private String updatedBy;
    private String updatedDateTime;
    private List<RoleDto> roles;
}
