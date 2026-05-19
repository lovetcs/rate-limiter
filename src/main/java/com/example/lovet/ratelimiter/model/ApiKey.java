import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyValue;
    
    // one client can have more than one apikey, hence the many to one
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;


}

