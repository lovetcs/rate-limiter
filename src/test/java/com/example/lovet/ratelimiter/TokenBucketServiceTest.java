import java.util.Optional;
import com.example.lovet.ratelimiter.model.RateLimitRule;
import com.example.lovet.ratelimiter.service.TokenBucketService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.example.lovet.ratelimiter.repository.RateLimitRuleRepository;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class TokenBucketServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RateLimitRuleRepository rateLimitRuleRepository;

    @InjectMocks
    private TokenBucketService tokenBucketService;

    @Test
    void shouldAllowRequestWhenTokensAvailable() {
        
        RateLimitRule testRateLimitRule = new RateLimitRule();
        testRateLimitRule.setRequestLimit(10);
        testRateLimitRule.setRefillRate(2);
        testRateLimitRule.setWindowSeconds(60);

        when(rateLimitRuleRepository.findByClient_Id(1L)).thenReturn(Optional.of(testRateLimitRule));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token_bucket:1:tokens")).thenReturn("5");
        when(valueOperations.get("token_bucket:1:lastRefill")).thenReturn(
        String.valueOf(Instant.now().getEpochSecond() - 10));

        boolean testResult = tokenBucketService.isAllowed(1L);
        
        assertTrue(testResult);
    }

    @Test
    void shouldDenyRequestWhenBucketEmpty() {
        RateLimitRule testRateLimitRule = new RateLimitRule();

        testRateLimitRule.setRequestLimit(10);
        testRateLimitRule.setRefillRate(2);
        testRateLimitRule.setWindowSeconds(60);

        when(rateLimitRuleRepository.findByClient_Id(1L)).thenReturn(Optional.of(testRateLimitRule));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token_bucket:1:tokens")).thenReturn("0");
        when(valueOperations.get("token_bucket:1:lastRefill")).thenReturn(
        String.valueOf(Instant.now().getEpochSecond()));

        boolean testResult = tokenBucketService.isAllowed(1L);
        
        assertFalse(testResult);
    }

    @Test
    void shouldAllowRequestAfterRefill() {
        RateLimitRule testRateLimitRule = new RateLimitRule();

        testRateLimitRule.setRequestLimit(10);
        testRateLimitRule.setRefillRate(2);
        testRateLimitRule.setWindowSeconds(60);

        when(rateLimitRuleRepository.findByClient_Id(1L)).thenReturn(Optional.of(testRateLimitRule));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token_bucket:1:tokens")).thenReturn("0");
        when(valueOperations.get("token_bucket:1:lastRefill")).thenReturn(
        String.valueOf(Instant.now().getEpochSecond() - 10));

        boolean testResult = tokenBucketService.isAllowed(1L);
        
        assertTrue(testResult);
    }
}