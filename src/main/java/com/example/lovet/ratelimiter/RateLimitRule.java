import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rate_limit_rules")
public class RateLimitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String algorithmType;
    
    @OneToOne
    private Client client;

    private Integer requestLimit;
    private Integer windowSeconds;
    private Integer refillRate;


}