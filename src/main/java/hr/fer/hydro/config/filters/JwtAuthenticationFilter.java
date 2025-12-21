package hr.fer.hydro.config.filters;

import hr.fer.hydro.enums.JWTType;
import hr.fer.hydro.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            final @NonNull HttpServletRequest request,
            final @NonNull HttpServletResponse response,
            final @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final JWTType tokenType = jwtService.getTokenType(jwt);
        final String username = switch (tokenType) {
            case ACCESS -> jwtService.extractUsernameFromAccessToken(jwt);
            case PENDING -> jwtService.extractUsernameFromPendingToken(jwt);
        };

        if (!Objects.isNull(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (Objects.isNull(userDetails)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "");
            }

            if (isTokenValid(jwt, tokenType, username)) {
                final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request, response);
        }

    }

    private boolean isTokenValid(final String jwt, final JWTType tokenType, final String username) {
        return switch (tokenType) {
            case ACCESS -> jwtService.isAccessTokenValid(jwt, username);
            case PENDING -> jwtService.isPendingTokenValid(jwt, username);
        };
    }
}
