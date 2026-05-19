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

    // creating the rate limit rule class, have got an id that will increment itself automatically in the db everytime its created
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String algorithmType;
    

    // one rule per client, cannot have more than one
    @OneToOne
    private Client client;

    // defining some of the variables that will be used for rules
    private Integer requestLimit;
    private Integer windowSeconds;
    private Integer refillRate;


}