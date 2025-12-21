package hr.fer.hydro;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("jwt")
public class HydroServiceProperties {
    @NotNull
    private String secretKey;
    @NotNull
    private String pendingSecretKey;
}
