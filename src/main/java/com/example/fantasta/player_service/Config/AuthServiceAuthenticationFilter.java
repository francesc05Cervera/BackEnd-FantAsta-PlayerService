package com.example.fantasta.player_service.Config;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import com.example.fantasta.player_service.Client.AuthServiceClient;
import com.example.fantasta.player_service.DTO.AuthUserResponse;
import com.example.fantasta.player_service.Exceptions.TokenException;

final class AuthServiceAuthenticationFilter extends OncePerRequestFilter {
    private final AuthServiceClient authServiceClient;
    AuthServiceAuthenticationFilter(AuthServiceClient authServiceClient) { this.authServiceClient = authServiceClient; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return HttpMethod.OPTIONS.matches(request.getMethod()) || "/api/ping".equals(request.getRequestURI()); }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            AuthUserResponse user = authServiceClient.getAuthenticatedUser(request.getHeader("Authorization"));
            String role = user.getRole() == null ? "USER" : user.getRole();
            SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
            chain.doFilter(request, response);
        } catch (TokenException exception) { writeError(response, 401, "Unauthorized", "Invalid or missing token");
        } catch (ResponseStatusException exception) { writeError(response, exception.getStatusCode().value(), exception.getStatusCode().toString(), exception.getReason());
        } finally { SecurityContextHolder.clearContext(); }
    }
    private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status); response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }
}
