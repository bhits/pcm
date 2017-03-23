package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.service.dto.PurposeDto;

import java.util.List;

public interface PurposeService {
    List<PurposeDto> getPurposes();
}
