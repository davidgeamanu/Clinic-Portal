package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationMessageStrategyRegistry {

    private final Map<NotificationType, NotificationMessageStrategy> strategies;

    public NotificationMessageStrategyRegistry(List<NotificationMessageStrategy> strategyBeans) {
        Map<NotificationType, NotificationMessageStrategy> map = new EnumMap<>(NotificationType.class);
        for (NotificationMessageStrategy strategy : strategyBeans) {
            map.put(strategy.type(), strategy);
        }
        this.strategies = map;
    }

    public NotificationMessageStrategy forType(NotificationType type) {
        NotificationMessageStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No NotificationMessageStrategy bean registered for type: " + type);
        }
        return strategy;
    }
}
