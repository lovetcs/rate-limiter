import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.lovet.ratelimiter.model.Client;
import com.example.lovet.ratelimiter.service.TokenBucketService;
import com.example.lovet.ratelimiter.service.SlidingWindowService;
import com.example.lovet.ratelimiter.repository.RateLimitRuleRepository;
import java.util.Optional;
import com.example.lovet.ratelimiter.model.ApiKey;
import com.example.lovet.ratelimiter.model.RateLimitRule;
import com.example.lovet.ratelimiter.repository.ApiKeyRepository;


// Request Api the base endpoint that everything goes through first
@RestController
@RequestMapping("/api")
public class RateLimiterController {


    //auto wired to finding the objects spring created at startup
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private RateLimitRuleRepository rateLimitRuleRepository;

    @Autowired
    private TokenBucketService tokenBucketService;

    @Autowired
    private SlidingWindowService slidingWindowService;

    @PostMapping("/check")
    public ResponseEntity<String> checkRateLimit(@RequestHeader("X-API-Key") String apiKey) {
        
        // Check if apikey is valid

        Optional<ApiKey> apiKeyFromDb = apiKeyRepository.findByKeyValue(apiKey);

        // checking if its actually in db before attempting get which would crash
        if (apiKeyFromDb.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid API key");
        }

        ApiKey apiKeyObj = apiKeyFromDb.get();

        Boolean isApiKeyActive = apiKeyObj.isActive();

        if (isApiKeyActive ==  false) {
            return ResponseEntity.status(401).body("API key is not active");
        }


        //Mistake made, was client is not just getClient but . getId as well. getClient only creates the client object as a whole have to look at the getters in that
        Long clientId = apiKeyObj.getClient().getId();

        // Get the type of rate limit

        Optional<RateLimitRule> rateLimitRulefromDb = rateLimitRuleRepository.findByClient_Id(clientId);

        if (rateLimitRulefromDb.isEmpty()) {
            return ResponseEntity.status(404).body("No rate limit rule found for client");
        }       

        RateLimitRule rateLimitRuleObj = rateLimitRulefromDb.get();

        String algorithmType = rateLimitRuleObj.getAlgorithmType();

        
        //Self explained in code, checks for type and returns
        if (algorithmType.equals("SLIDING_WINDOW")) {
            boolean slidingWindowAllowedRequest = slidingWindowService.isAllowed(clientId);

            if (slidingWindowAllowedRequest) {
                return ResponseEntity.ok("Request allowed");
            } else {
                return ResponseEntity.status(429).body("Rate limit exceeded");
            }

        } else if (algorithmType.equals("TOKEN_BUCKET")) {
            boolean tokenBucketAllowedRequest = tokenBucketService.isAllowed(clientId);

            if (tokenBucketAllowedRequest) {
                return ResponseEntity.ok("Request allowed");
            } else {
                return ResponseEntity.status(429).body("Rate limit exceeded");
            }
        } else {
            return ResponseEntity.status(401).body("Invalid algorithm type");
        }
        
    }
}