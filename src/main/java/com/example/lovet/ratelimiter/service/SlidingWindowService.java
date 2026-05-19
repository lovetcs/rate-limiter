import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.lovet.ratelimiter.model.RateLimitRule;
import com.example.lovet.ratelimiter.repository.RateLimitRuleRepository;


@Service
public class SlidingWindowService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RateLimitRuleRepository rateLimitRuleRepository;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean isAllowed(Long clientId) {

        lock.lock();

        try {

            Optional<RateLimitRule> rule = rateLimitRuleRepository.findByClient_Id(clientId);

            if (rule.isEmpty()) {
                return false;
            }

            RateLimitRule ruleObj = rule.get();
            Integer maxRequests = ruleObj.getRequestLimit();
            Integer windowSeconds = ruleObj.getWindowSeconds();

            String tokenWindowKey = "sliding_window:" + clientId + ":requests";

            Long currentTime = Instant.now().toEpochMilli();
            

            Long cutoffTime = currentTime - (windowSeconds * 1000L);

            redisTemplate.opsForZSet().removeRangeByScore(tokenWindowKey, 0, cutoffTime);

            redisTemplate.opsForZSet().add(tokenWindowKey, String.valueOf(currentTime), currentTime);

            Long requestsInWindow = redisTemplate.opsForZSet().zCard(tokenWindowKey);

            if (requestsInWindow >= maxRequests) {
                return false;
            } else {
                return true;
            }
           

        } finally {
            lock.unlock();
        }

    }

}