package hr.fer.hydro.config.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import hr.fer.hydro.service.google.auth.impl.GoogleAuthCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GoogleAuthConfig {
    private final GoogleAuthCredentialRepository googleAuthCredentialRepository;
    @Bean
    public GoogleAuthenticator getGoogleAuthenticator(){
        final GoogleAuthenticator gAuth = new GoogleAuthenticator();
        gAuth.setCredentialRepository(googleAuthCredentialRepository);
        return gAuth;
    }


}
