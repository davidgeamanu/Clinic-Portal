package com.clinic.portal.service.appointment.state;

import com.clinic.portal.model.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Looks up the {@link AppointmentState} bean responsible for a given
 * {@link AppointmentStatus}.
 */
@Component
public class AppointmentStateRegistry {

    private final Map<AppointmentStatus, AppointmentState> states;

    public AppointmentStateRegistry(List<AppointmentState> stateBeans) {
        Map<AppointmentStatus, AppointmentState> map = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentState state : stateBeans) {
            map.put(state.status(), state);
        }
        this.states = map;
    }

    public AppointmentState forStatus(AppointmentStatus status) {
        AppointmentState state = states.get(status);
        if (state == null) {
            throw new IllegalStateException("No AppointmentState bean registered for status: " + status);
        }
        return state;
    }
}