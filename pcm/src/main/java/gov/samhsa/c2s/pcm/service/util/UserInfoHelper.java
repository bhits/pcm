package gov.samhsa.c2s.pcm.service.util;

import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;

public class UserInfoHelper {

    private static final String SPACE_PATTERN = " ";

    public static String getFullName(String firstName, String middleName, String lastName) {
        return firstName
                .concat(getMiddleName(middleName))
                .concat(SPACE_PATTERN + lastName);
    }

    public static String getUserFullName(UserDto userDto) {
        return getFullName(userDto.getFirstName(), userDto.getMiddleName(), userDto.getLastName());
    }

    private static String getMiddleName(String middleName) {
        if (middleName == null) {
            return "";
        } else {
            return SPACE_PATTERN.concat(middleName);
        }
    }
}
