package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.PurposeService;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PurposeRestController {

    @Autowired
    private PurposeService purposeService;

    @GetMapping("/purposes")
    public List<PurposeDto> getPurposes() {
        return purposeService.getPurposes();
    }
}