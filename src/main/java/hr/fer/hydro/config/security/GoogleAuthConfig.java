package hr.fer.hydro.config.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.KeyRepresentation;
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
        final GoogleAuthenticator gAuth = new GoogleAuthenticator(getGoogleAuthenticatorConfig());
        gAuth.setCredentialRepository(googleAuthCredentialRepository);
        return gAuth;
    }

    private GoogleAuthenticatorConfig getGoogleAuthenticatorConfig(){
        return new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setCodeDigits(6)
                .setNumberOfScratchCodes(8)
                .setKeyRepresentation(KeyRepresentation.BASE64)
                .build();
    }
}
