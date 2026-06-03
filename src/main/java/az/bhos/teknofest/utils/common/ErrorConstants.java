package az.bhos.teknofest.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorConstants {

    public static final String USER_NOT_FOUND = "User not found!";

    public static final String PENDING_VERIFICATION_NOT_FOUND = "Pending verification not found!";

    public static final String EMAIL_ALREADY_EXISTS = "Email already exists!";

    public static final String VERIFICATION_TOKEN_EXPIRED = "Verification code expired!";

    public static final String INVALID_VERIFICATION_TOKEN = "Invalid verification code!";
}
