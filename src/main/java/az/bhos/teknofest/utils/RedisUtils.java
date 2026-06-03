package az.bhos.teknofest.utils;

public final class RedisUtils {

    private RedisUtils() {
    }

    public static final String REGISTER_PREFIX = "auth:register:email:";

    public static String registerKey(String email) {
        return REGISTER_PREFIX + email;
    }
}
