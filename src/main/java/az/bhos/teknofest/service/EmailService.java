package az.bhos.teknofest.service;

import az.bhos.teknofest.model.event.NotificationEvent;

public interface EmailService {
    void sendNotification(NotificationEvent notificationEvent);
}
