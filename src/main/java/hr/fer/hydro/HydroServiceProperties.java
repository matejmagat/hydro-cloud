package hr.fer.hydro;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("jwt")
public class HydroServiceProperties {
    private String secretKey;
    public String pendingSecretKey;
}
