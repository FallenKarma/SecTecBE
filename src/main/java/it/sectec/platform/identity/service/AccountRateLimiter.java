package it.sectec.platform.identity.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import it.sectec.platform.shared.crud.CrudException;

@Component
public class AccountRateLimiter {
    private record Window(long start, int count) {}
    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;
    public AccountRateLimiter(Clock clock) { this.clock = clock; }
    public synchronized void check(String address) {
        long now = clock.millis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().start() >= 60_000);
        var window = windows.get(address);
        if ((window != null && window.count() >= 10) || (window == null && windows.size() >= 10_000)) {
            org.slf4j.LoggerFactory.getLogger(AccountRateLimiter.class).warn("Account request rate limited: reason={}",
                    window != null ? "IP_WINDOW_EXHAUSTED" : "LIMITER_CAPACITY_REACHED");
            throw new CrudException(429, "TOO_MANY_REQUESTS");
        }
        windows.put(address, window == null ? new Window(now, 1) : new Window(window.start(), window.count() + 1));
    }
}

