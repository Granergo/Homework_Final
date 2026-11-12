package com.linkshortener.notification;

import java.util.logging.Logger;

public class NotificationService {

    private static final Logger logger =
            Logger.getLogger(NotificationService.class.getName());

    public void publish(NotificationEvent event) {

        String message = switch (event.getType()) {

            case LINK_EXPIRED ->
                    "Истёк срок действия ссылки";

            case CLICK_LIMIT_REACHED ->
                    "Исчерпан лимит переходов";

            case SCHEDULER_ERROR ->
                    "Ошибка планировщика очистки";
        };

        String owner = event.getOwnerId() == null
                ? "не указан"
                : event.getOwnerId().toString();

        String code = event.getShortCode() == null
                ? "не указан"
                : event.getShortCode();

        logger.info(() -> String.format(
                """
                ===== СОБЫТИЕ LINK SHORTENER =====
                Тип: %s
                Время: %s
                Владелец: %s
                Короткий код: %s
                Сообщение: %s
                =================================
                """,
                event.getType(),
                event.getOccurredAt(),
                owner,
                code,
                event.getMessage() == null ? message : event.getMessage()
        ));
    }
}