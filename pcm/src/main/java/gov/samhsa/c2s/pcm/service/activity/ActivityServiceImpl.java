package gov.samhsa.c2s.pcm.service.activity;

import gov.samhsa.c2s.pcm.config.ActivityProperties;
import gov.samhsa.c2s.pcm.domain.PatientRepository;
import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.RoleDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.infrastructure.jdbcpaging.JdbcPagingRepository;
import gov.samhsa.c2s.pcm.service.dto.ConsentActivityDto;
import gov.samhsa.c2s.pcm.service.exception.ActivityNotFoundException;
import gov.samhsa.c2s.pcm.service.exception.NoMatchRoleFoundException;
import gov.samhsa.c2s.pcm.service.exception.PatientNotFoundException;
import gov.samhsa.c2s.pcm.service.util.UserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    private static final String TYPE_OF_ACTIVITIES = "consent-activity";

    private final ActivityProperties activityProperties;
    private final JdbcPagingRepository jdbcPagingRepository;
    private final PatientRepository patientRepository;
    private final UmsService umsService;
    private final MessageSource messageSource;

    @Autowired
    public ActivityServiceImpl(ActivityProperties activityProperties,
                               JdbcPagingRepository jdbcPagingRepository,
                               PatientRepository patientRepository,
                               UmsService umsService, MessageSource messageSource) {
        this.activityProperties = activityProperties;
        this.jdbcPagingRepository = jdbcPagingRepository;
        this.patientRepository = patientRepository;
        this.umsService = umsService;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsentActivityDto> getConsentActivities(String patientId, Optional<Integer> page, Optional<Integer> size) {
        assertPatientExist(patientId);

        ActivityProperties.Activity consentActivity = activityProperties.getActivities().stream()
                .filter(activity -> activity.type.equalsIgnoreCase(TYPE_OF_ACTIVITIES))
                .findAny()
                .orElseThrow(ActivityNotFoundException::new);

        Sort.Direction sortDirection = Optional.of(consentActivity.getSortBy().getDirection()).orElse(Sort.Direction.ASC);
        final PageRequest pageRequest = new PageRequest(
                page.filter(p -> p >= 0).orElse(0),
                size.filter(s -> s > 0).orElse(consentActivity.getPagination().getDefaultSize()),
                sortDirection,
                consentActivity.getSortBy().getProperty());
        Page<ConsentActivityQueryResult> pagedActivityQueryResult = jdbcPagingRepository.findAllByArgs(consentActivity.getSql().getFilePath(), pageRequest, patientId);
        return mapConsentActivityQueryResultToConsentActivityDto(pagedActivityQueryResult);
    }

    private Page<ConsentActivityDto> mapConsentActivityQueryResultToConsentActivityDto(Page<ConsentActivityQueryResult> pagedActivityQueryResult) {
        return pagedActivityQueryResult.map(pagedActivityQueryResult1 -> ConsentActivityDto.builder()
                .consentReferenceId(pagedActivityQueryResult1.getConsentReferenceId())
                .actionType(getConsentActivityActionType(pagedActivityQueryResult1.getConsentStage(), pagedActivityQueryResult1.getRevType()))
                .updatedBy(UserInfoHelper.getUserFullName(getUserByAuthId(pagedActivityQueryResult1.getLastUpdatedBy())))
                .updatedDateTime(pagedActivityQueryResult1.getLastUpdatedDateTime())
                .role(determineUserRole(pagedActivityQueryResult1))
                .build());
    }

    private RoleDto determineUserRole(ConsentActivityQueryResult pagedActivityQueryResult) {
        //TODO: determine a single role in current login user
        return getUserByAuthId(pagedActivityQueryResult.getLastUpdatedBy()).getRoles().stream()
                .findAny()
                .orElseThrow(NoMatchRoleFoundException::new);
    }

    private UserDto getUserByAuthId(String userAuthId) {
        return umsService.getUserById(userAuthId);
    }

    private String getConsentActivityActionType(String consentStage, String revType) {
        switch (revType) {
            case "0":
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.CREATE_CONSENT");
            case "1":
                return determineUpdatedConsentActionType(consentStage);
            case "2":
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.DELETE_CONSENT");
            default:
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.INVALID_ACTION_TYPE");
        }
    }

    private String translateToSelectedLocale(String messageCode) {
        Locale selectedLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageCode, null, selectedLocale);
    }

    private String determineUpdatedConsentActionType(String consentStage) {
        switch (ConsentStage.valueOf(consentStage)) {
            case SIGNED:
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.SIGN_CONSENT");
            case REVOKED:
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.REVOKE_CONSENT");
            case DELETED:
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.DELETE_CONSENT");
            default:
                return translateToSelectedLocale("ACTIVITY.ACTION_TYPE.EDIT_CONSENT");
        }
    }

    private void assertPatientExist(String patientId) {
        if (!patientRepository.findOneById(patientId).isPresent()) {
            log.error("Patient NOT Found by patientId: " + patientId);
            throw new PatientNotFoundException("Patient NOT Found!");
        }
    }
}
