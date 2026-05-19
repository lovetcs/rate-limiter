import java.time.Instant;
import org.springframework.stereotype.Service;                         // Added import
import org.springframework.beans.factory.annotation.Autowired;
import com.example.lovet.ratelimiter.model.Client;
import com.example.lovet.ratelimiter.model.RateLimitRule;

@Service
public class TokenBucketService {
    
    // Autowired to inject the dependicies, finds redisTemplate so i dont have to
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Same thing here, auto wired just finds the objects spring created at startup and links it to here
    @Autowired
    private RateLimitRuleRepository rateLimitRuleRepository;
    private Client client;

    // isalloed method, gives in client id and will have give back true or false
    public Boolean isAllowed(Long clientId) {
        
        // just finds the rules of the client by its id in db
        RateLimitRule rule = rateLimitRuleRepository.findByClient_Id(clientId);
         
        // if theres no rules (which means its invalid) it will straight return false
        if (rule == null) {
            return false; 
        }
        

        // initialising variables received from the rules so we know ours limits
        Integer initialTokens = rule.getRequestLimit();
        Integer windowSeconds = rule.getWindowSeconds();
        Integer refillRate = rule.getRefillRate();

        // storing the redis calls in var keys so do not have to repeat the concat

        String tokenBucketKey = "token_bucket:" + clientId + ":tokens";
        String lastRefillKey = "token_bucket:" + clientId + ":lastRefill";

        String currentTokenStr = redisTemplate.opsForValue().get(tokenBucketKey);

        // If the tokenBucket string is not in redis then its going to create an entry then auto return true since its fresh
        if (currentTokenStr == null) {

            // Gets time in long format
            Long currentTime = Instant.now().getEpochSecond();

            redisTemplate.opsForValue().set(tokenBucketKey, String.valueOf(initialTokens));
            redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(currentTime));
            
            return true;

        } else {
            

            // Valid entry in redis so we calculate if its over liit or not
            Integer currentTokens = Integer.parseInt(currentTokenStr);
            Long lastRefilledTime = Long.valueOf(redisTemplate.opsForValue().get(lastRefillKey));
            Long currentTime = Instant.now().getEpochSecond();
            Long secondsBetween = currentTime - lastRefilledTime;

            // Lazy refilling, dont have to keep an actual ongoing count of all TokenBucketService
            
            Long tokensToAdd = (secondsBetween * refillRate) / windowSeconds;
            int newTokenBalance = (int) tokensToAdd + currentTokens;

            if (newTokenBalance > (int) initialTokens) {
                newTokenBalance = initialTokens;
            }
            //there is still allowed tokens, so calculate new balance by decrementing
            if (newTokenBalance > 0) {
                
                newTokenBalance--;

                redisTemplate.opsForValue().set(tokenBucketKey, String.valueOf(newTokenBalance));
                redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(currentTime));

                return true;
            } else {
                return false;
            }
        }
    }
}
