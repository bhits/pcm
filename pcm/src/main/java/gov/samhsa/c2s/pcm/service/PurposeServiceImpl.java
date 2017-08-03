package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

@Service
public class PurposeServiceImpl implements PurposeService {
    private final ModelMapper modelMapper;
    private final PurposeRepository purposeRepository;
    private I18nService i18nService;

    @Autowired
    public PurposeServiceImpl(ModelMapper modelMapper, PurposeRepository purposeRepository, I18nService i18nService) {
        this.modelMapper = modelMapper;
        this.purposeRepository = purposeRepository;
        this.i18nService = i18nService;
    }

    @Override
    public List<PurposeDto> getPurposes(Locale locale) {
        final List<Purpose> purposes = purposeRepository.findAll();

        purposes.stream().forEach(purpose -> {
            purpose.setDisplay(i18nService.getPurposeOfUseI18nDisplay(purpose.getIdentifier().getValue()));
            purpose.setDescription(i18nService.getPurposeOfUseI18nDescription(purpose.getIdentifier().getValue()));
        });

        return purposes.stream()
                .map(purpose -> modelMapper.map(purpose, PurposeDto.class))
                .collect(toList());
    }
}