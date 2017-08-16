package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.common.i18n.service.I18nService;
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
    private I18nService i18nService;

    @Autowired
    public PurposeServiceImpl(ModelMapper modelMapper, PurposeRepository purposeRepository, I18nService i18nService) {
        this.modelMapper = modelMapper;
        this.purposeRepository = purposeRepository;
        this.i18nService = i18nService;
    }

    @Override
    public List<PurposeDto> getPurposes() {
        final List<Purpose> purposes = purposeRepository.findAll();

        return purposes.stream()
                .map(purpose -> {
                    final PurposeDto purposeDto = modelMapper.map(purpose, PurposeDto.class);
                    purposeDto.setDisplay(i18nService.getI18nMessage(purpose, "display", purpose::getDisplay));
                    purposeDto.setDescription(i18nService.getI18nMessage(purpose, "description", purpose::getDescription));
                    return purposeDto;
                })
                .collect(toList());
    }
}