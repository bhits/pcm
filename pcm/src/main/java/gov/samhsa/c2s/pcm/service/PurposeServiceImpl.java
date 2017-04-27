package gov.samhsa.c2s.pcm.service;

import gov.samhsa.c2s.pcm.domain.Purpose;
import gov.samhsa.c2s.pcm.domain.PurposeRepository;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

@Service
public class PurposeServiceImpl implements PurposeService {
    private final ModelMapper modelMapper;
    private final PurposeRepository purposeRepository;
    private final static String DISPLAY = ".DISPLAY";
    private final static String DESCRIPTION = ".DESCRIPTION";
    private final static String PURPOSE = "PURPOSE.";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    public PurposeServiceImpl(ModelMapper modelMapper, PurposeRepository purposeRepository) {
        this.modelMapper = modelMapper;
        this.purposeRepository = purposeRepository;
    }

    @Override
    public List<PurposeDto> getPurposes(Locale locale) {
        final List<Purpose> purposes = purposeRepository.findAll();
        return purposes.stream()
                .peek(purpose -> {
                    purpose.setDisplay(messageSource.getMessage(composePurposeMsgKey(purpose.getIdentifier().getValue(), DISPLAY),null, locale ));
                    purpose.setDescription(messageSource.getMessage(composePurposeMsgKey(purpose.getIdentifier().getValue(),DESCRIPTION),null, locale ));
                })
                .map(purpose -> modelMapper.map(purpose, PurposeDto.class))
                .collect(toList());
    }

    private String composePurposeMsgKey(String value, String property){
        return PURPOSE + value + property;
    }
}