package hr.fer.hydro.service;

import hr.fer.hydro.enums.JWTType;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {
    JWTType getTokenType(String jwt);

    String generateAccessToken(Map<String, Object> claims, UserDetails userDetails);

    String generateAccessToken(UserDetails userDetails);

    String generatePendingToken(UserDetails userDetails);

    String generatePendingToken(Map<String, Object> claims, UserDetails userDetails);


    boolean isAccessTokenValid(String jwt, String username);

    boolean isAccessTokenExpired(String jwt);

    String extractUsernameFromAccessToken(String jwt);

    Integer extractUserIdFromAccessToken(String jwt);

    boolean isPendingTokenValid(String jwt, String username);

    boolean isPendingTokenExpired(String jwt);

    String extractUsernameFromPendingToken(String jwt);

    Integer extractUserIdFromPendingToken(String jwt);
}
