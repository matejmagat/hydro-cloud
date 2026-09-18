package hr.fer.hydro.config.filters;

import hr.fer.hydro.auth.auth.service.JwtService;
import hr.fer.hydro.auth.persistence.enums.JWTType;
import hr.fer.hydro.config.core.UserCoreLocalThread;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AddCoreUserFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (Objects.isNull(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final JWTType tokenType = jwtService.getTokenType(jwt);
        if (tokenType == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final Integer userId = switch (tokenType) {
            case ACCESS -> jwtService.extractUserIdFromAccessToken(jwt);
            case PENDING -> jwtService.extractUserIdFromPendingToken(jwt);
        };

        UserCoreLocalThread.setUserInfo(userId);
        filterChain.doFilter(request, response);
        UserCoreLocalThread.deleteUserInfo();
    }
}
