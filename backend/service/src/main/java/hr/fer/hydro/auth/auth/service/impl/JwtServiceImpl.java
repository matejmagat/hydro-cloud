package hr.fer.hydro.auth.auth.service.impl;

import hr.fer.hydro.HydroServiceProperties;
import hr.fer.hydro.auth.auth.service.JwtService;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.enums.JWTType;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final UserDao userDao;

    private final HydroServiceProperties properties;
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;
    private static final long PENDING_TOKEN_EXPIRATION = 1000 * 60 * 5;

    @Override
    public String generateAccessToken(final UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    @Override
    public JWTType getTokenType(String jwt) {
        for (final JWTType tokenType : JWTType.values()) {
            try {
                final Claims claims = extractAllClaims(jwt, tokenType);
                validateTokenType(claims, tokenType);
                return tokenType;
            } catch (final Exception ignore) {
            }
        }
        return null;
    }

    @Override
    public String generateAccessToken(final Map<String, Object> claims, final UserDetails userDetails) {
        final String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", authorities);

        return buildToken(claims, userDetails, JWTType.ACCESS, ACCESS_TOKEN_EXPIRATION);
    }

    @Override
    public String generatePendingToken(final UserDetails userDetails) {
        return generatePendingToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generatePendingToken(final Map<String, Object> claims, final UserDetails userDetails) {
        return buildToken(claims, userDetails, JWTType.PENDING, PENDING_TOKEN_EXPIRATION);
    }

    @Override
    public String extractUsernameFromAccessToken(final String jwt) {
        return extractClaim(jwt, JWTType.ACCESS, Claims::getSubject);
    }

    @Override
    public Integer extractUserIdFromAccessToken(final String jwt) {
        return extractClaim(jwt, JWTType.ACCESS, claims -> claims.get("id", Integer.class));
    }

    @Override
    public boolean isAccessTokenValid(final String jwt, String username) {
        return isTokenValid(jwt, username, JWTType.ACCESS);
    }

    @Override
    public boolean isAccessTokenExpired(String jwt) {
        return isTokenExpired(jwt, JWTType.ACCESS);
    }

    @Override
    public String extractUsernameFromPendingToken(final String jwt) {
        return extractClaim(jwt, JWTType.PENDING, Claims::getSubject);
    }

    @Override
    public Integer extractUserIdFromPendingToken(final String jwt) {
        final String username = extractUsernameFromPendingToken(jwt);
        final UserEntity user = userDao.findByUsername(username).orElseThrow();
        return user.getId();
    }

    @Override
    public boolean isPendingTokenValid(final String jwt, final String username) {
        return isTokenValid(jwt, username, JWTType.PENDING);
    }

    @Override
    public boolean isPendingTokenExpired(final String jwt) {
        return isTokenExpired(jwt, JWTType.PENDING);
    }

    private String buildToken(final Map<String, Object> claims, final UserDetails userDetails,
                              final JWTType tokenType, final long expirationTime) {
        claims.put("token-type", tokenType.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(tokenType), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenValid(final String jwt, final String username, final JWTType tokenType) {
        try {
            final String extractedUsername = extractClaim(jwt, tokenType, Claims::getSubject);
            return extractedUsername.equals(username) && !isTokenExpired(jwt, tokenType);
        } catch (final JwtException e) {
            log.warn("Token validation failed for type {}: {}", tokenType, e.getMessage());
            return false;
        } catch (final BadCredentialsException badCredentialsException) {
            return false;
        }
    }

    private boolean isTokenExpired(String jwt, JWTType tokenType) {
        try {
            final Date expiration = extractClaim(jwt, tokenType, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (final BadCredentialsException e) {
            return true;
        }
    }

    private <T> T extractClaim(final String jwt, final JWTType tokenType, final Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(jwt, tokenType);
            validateTokenType(claims, tokenType);
            return claimsResolver.apply(claims);
        } catch (final ExpiredJwtException e) {
            throw new BadCredentialsException(tokenType.name() + " token has expired");
        } catch (final JwtException e) {
            throw new BadCredentialsException("Invalid " + tokenType.name() + " token: " + e.getMessage());
        }
    }

    private Claims extractAllClaims(final String jwt, final JWTType tokenType) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(tokenType))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private void validateTokenType(final Claims claims, final JWTType expectedType) {
        final String tokenType = claims.get("token-type", String.class);
        if (tokenType == null || !tokenType.equals(expectedType.name())) {
            throw new BadCredentialsException(
                    String.format("Invalid token type. Expected: %s, Found: %s",
                            expectedType.name(), tokenType)
            );
        }
    }

    private Key getSigningKey(final JWTType tokenType) {
        final String secretKey = switch (tokenType) {
            case ACCESS -> properties.getSecretKey();
            case PENDING -> properties.getPendingSecretKey();
        };

        final byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}