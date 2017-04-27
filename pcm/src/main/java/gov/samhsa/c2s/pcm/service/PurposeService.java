package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

public interface PurposeService {

    @Transactional(readOnly = true)
    List<PurposeDto> getPurposes(Locale locale);
}
