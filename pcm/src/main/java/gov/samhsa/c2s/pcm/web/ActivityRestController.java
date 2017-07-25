package gov.samhsa.c2s.pcm.web;

import gov.samhsa.c2s.pcm.service.activity.ActivityService;
import gov.samhsa.c2s.pcm.service.dto.ConsentActivityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/patients/{patientId}")
public class ActivityRestController {

    private final ActivityService activityService;

    @Autowired
    public ActivityRestController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/consent-activities")
    public Page<ConsentActivityDto> getConsentActivities(@PathVariable String patientId,
                                                         @RequestParam Optional<Integer> page,
                                                         @RequestParam Optional<Integer> size) {
        return activityService.getConsentActivities(patientId, page, size);
    }
}