package com.clinic.notificationservice.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks open Server-Sent-Events connections per user and pushes new
 * notifications to them in real time (no polling).
 *
 * A user may hold several connections at once (multiple tabs); stale
 * connections are pruned when a send fails, on timeout, and by the
 * heartbeat sweep.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    /** 30 minutes; the browser's EventSource reconnects automatically. */
    private static final long TIMEOUT_MS = 30L * 60 * 1000;

    private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            remove(userId, emitter);
        }

        log.debug("SSE subscribed: userId={} ({} open connections)", userId, count(userId));
        return emitter;
    }

    public void push(Long userId, Object payload) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(payload));
            } catch (Exception e) {
                remove(userId, emitter);
            }
        }
    }

    /** Keeps intermediaries (dev proxy, reverse proxy) from closing idle streams. */
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        emittersByUser.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (Exception e) {
                    remove(userId, emitter);
                }
            }
        });
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId, emitters);
        }
    }

    private int count(Long userId) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        return emitters == null ? 0 : emitters.size();
    }
}
