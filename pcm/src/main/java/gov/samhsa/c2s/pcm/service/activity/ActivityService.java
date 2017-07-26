package gov.samhsa.c2s.pcm.service.activity;

import gov.samhsa.c2s.pcm.service.dto.ConsentActivityDto;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ActivityService {
    @Transactional(readOnly = true)
    Page<ConsentActivityDto> getConsentActivities(String patientId, Optional<Integer> page, Optional<Integer> size);
}
