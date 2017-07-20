package gov.samhsa.c2s.pcm.service.activity;

import gov.samhsa.c2s.pcm.config.ActivityProperties;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.jdbcsupport.JdbcPagingRepository;
import gov.samhsa.c2s.pcm.service.dto.ConsentActivityDto;
import gov.samhsa.c2s.pcm.service.exception.PatientNotFoundException;
import gov.samhsa.c2s.pcm.service.util.UserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    private static final int INDEX_OF_CONSENT_ACTIVITY = 1;

    private final ActivityProperties activityProperties;
    private final JdbcPagingRepository jdbcPagingRepository;
    private final PatientRepository patientRepository;
    private final UmsService umsService;

    @Autowired
    public ActivityServiceImpl(ActivityProperties activityProperties,
                               JdbcPagingRepository jdbcPagingRepository,
                               PatientRepository patientRepository,
                               UmsService umsService) {
        this.activityProperties = activityProperties;
        this.jdbcPagingRepository = jdbcPagingRepository;
        this.patientRepository = patientRepository;
        this.umsService = umsService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsentActivityDto> getConsentActivities(String patientId, Optional<Integer> page, Optional<Integer> size) {
        assertPatientExist(patientId);
        final PageRequest pageRequest = new PageRequest(page.filter(p -> p >= 0).orElse(0),
                size.filter(s -> s > 0).orElse(activityProperties.getActivity().getPagination().getDefaultSize()));
        Page<ConsentActivityQueryResult> pagedActivityQueryResult = jdbcPagingRepository.findAllByArgs(INDEX_OF_CONSENT_ACTIVITY, pageRequest, patientId);
        return mapConsentActivityQueryResultToConsentActivityDto(pagedActivityQueryResult);
    }

    private Page<ConsentActivityDto> mapConsentActivityQueryResultToConsentActivityDto(Page<ConsentActivityQueryResult> pagedActivityQueryResult) {
        return pagedActivityQueryResult.map(pagedActivityQueryResult1 -> ConsentActivityDto.builder()
                .consentReferenceId(pagedActivityQueryResult1.getConsentReferenceId())
                .actionType(getConsentActivityActionType(pagedActivityQueryResult1.getConsentStage(), pagedActivityQueryResult1.getRevType()))
                .updatedBy(UserInfoHelper.getUserFullName(getUserByAuthId(pagedActivityQueryResult1.getLastUpdatedBy())))
                .updatedDateTime(pagedActivityQueryResult1.getLastUpdatedDateTime())
                .roles(getUserByAuthId(pagedActivityQueryResult1.getLastUpdatedBy()).getRoles())
                .build());
    }

    private UserDto getUserByAuthId(String userAuthId) {
        return umsService.getUserById(userAuthId);
    }

    private String getConsentActivityActionType(String consentStage, String revType) {
        String actionType;
        switch (revType) {
            case "0":
                actionType = "Create Consent";
                break;
            case "1":
                actionType = determineUpdatedConsentActionType(consentStage);
                break;
            case "2":
                actionType = "Delete Consent";
                break;
            default:
                actionType = "Invalid Action Type.";
        }
        return actionType;
    }

    private String determineUpdatedConsentActionType(String consentStage) {
        switch (ConsentStage.valueOf(consentStage)) {
            case SIGNED:
                return "Sign Consent";
            case REVOKED:
                return "Revoke Consent";
            default:
                return "Edit Consent";
        }
    }

    private void assertPatientExist(String patientId) {
        if (!patientRepository.findOneById(patientId).isPresent()) {
            log.error("Patient NOT Found by patientId: " + patientId);
            throw new PatientNotFoundException("Patient NOT Found!");
        }
    }
}
