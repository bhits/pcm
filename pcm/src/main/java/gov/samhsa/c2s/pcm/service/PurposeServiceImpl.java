package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PurposeServiceImpl implements PurposeService {
    @Autowired
    private PurposeRepository purposeRepository;

    @Override
    public List<PurposeDto> getPurposes() {
        final List<Purpose> purposes = purposeRepository.findAll();
        return purposes.stream()
                .map(purpose -> PurposeDto.builder()
                        .id(purpose.getId())
                        .display(purpose.getDisplay())
                        .system(purpose.getIdentifier().getSystem())
                        .value(purpose.getIdentifier().getValue())
                        .build())
                .collect(toList());
    }
}