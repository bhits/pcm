package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.I18nMessage;
import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

        purposes.stream().forEach(purpose -> {

            Optional<I18nMessage> displayMessageOptional = i18nService.getI18nMessage(purpose,"DISPLAY" );
            if(displayMessageOptional.isPresent()){
                purpose.setDisplay(displayMessageOptional.get().getMessage());
            }

            Optional<I18nMessage> descriptionMessageOptional = i18nService.getI18nMessage(purpose,"DESCRIPTION" );
            if(descriptionMessageOptional.isPresent()){
                purpose.setDescription(descriptionMessageOptional.get().getMessage());
            }
        });

        return purposes.stream()
                .map(purpose -> modelMapper.map(purpose, PurposeDto.class))
                .collect(toList());
    }
}