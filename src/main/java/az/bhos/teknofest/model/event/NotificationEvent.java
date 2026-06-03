package az.bhos.teknofest.model.event;

import az.bhos.teknofest.utils.enums.NotificationType;
import java.util.Map;

public record NotificationEvent(
        String to,
        NotificationType type,
        Map<String, Object> params) {
}
