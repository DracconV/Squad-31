package br.com.seed.notificacoes.security;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHelper {

    public UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Usuario autenticado ausente");
        }
        return UUID.fromString(authentication.getName());
    }

    public Set<String> getAuthenticatedRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }

    public boolean isInstitutionalOperator() {
        Set<String> roles = getAuthenticatedRoles();
        return roles.contains("ADMIN_ESCOLA") || roles.contains("ADMIN_SEED");
    }
}
