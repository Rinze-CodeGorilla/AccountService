package account;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BruteForceCounter {
    private final Map<String, Integer> map = new HashMap<>();

    public boolean isBlocked(String email) {
        return map.getOrDefault(email, 0) >= 5;
    }

    public void increment(String email) {
        map.put(email, map.getOrDefault(email, 0) + 1);
    }

    public void reset(String email) {
        map.remove(email);
    }
}
