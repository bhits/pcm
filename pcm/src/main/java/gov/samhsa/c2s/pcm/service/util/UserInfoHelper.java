package gov.samhsa.c2s.pcm.service.util;

import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;

public class UserInfoHelper {

    private static final String SPACE_PATTERN = " ";

    public static String getUserFullName(UserDto userDto) {
        return userDto.getFirstName()
                .concat(getMiddleName(userDto.getMiddleName()))
                .concat(SPACE_PATTERN + userDto.getLastName());
    }

    private static String getMiddleName(String middleName) {
        if (middleName == null) {
            return "";
        } else {
            return SPACE_PATTERN.concat(middleName);
        }
    }
}
