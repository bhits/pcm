package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PurposeServiceImpl implements PurposeService {
    private final ModelMapper modelMapper;
    private final PurposeRepository purposeRepository;

    @Autowired
    public PurposeServiceImpl(ModelMapper modelMapper, PurposeRepository purposeRepository) {
        this.modelMapper = modelMapper;
        this.purposeRepository = purposeRepository;
    }

    @Override
    public List<PurposeDto> getPurposes() {
        final List<Purpose> purposes = purposeRepository.findAll();
        return purposes.stream()
                .map(purpose -> modelMapper.map(purpose, PurposeDto.class))
                .collect(toList());
    }
}