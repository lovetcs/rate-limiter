import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.lovet.ratelimiter.model.ApiKey;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValue(String keyValue);
}