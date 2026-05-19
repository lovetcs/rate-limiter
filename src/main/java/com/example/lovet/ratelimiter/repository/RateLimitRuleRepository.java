import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.lovet.ratelimiter.model.RateLimitRule;

@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, Long> {

    Optional<RateLimitRule> findByClient_Id(Long clientId);
}