package it.sectec.platform.security;

import it.sectec.platform.identity.repository.*;
import java.util.TreeSet;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseUserDetailsService.class);
    private final UserRepository users;
    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;

    public DatabaseUserDetailsService(UserRepository users, UserRoleRepository userRoles,
                                      RolePermissionRepository rolePermissions) {
        this.users = users;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> {
                    log.debug("Login lookup rejected: account not found");
                    return new UsernameNotFoundException("Credenziali non valide");
                });
        if (!"ACTIVE".equals(user.getStatus())) {
            log.debug("Login account is not active: pendingVerification={} locked={}",
                    "PENDING_VERIFICATION".equals(user.getStatus()), "LOCKED".equals(user.getStatus()));
        }
        var roles = new TreeSet<String>();
        var permissions = new TreeSet<String>();
        for (var assignment : userRoles.findByUser_Id(user.getId())) {
            var role = assignment.getRole();
            roles.add(role.getName());
            rolePermissions.findByRole_Id(role.getId())
                    .forEach(link -> permissions.add(link.getPermission().getName()));
        }
        return new AccountPrincipal(new AccountPrincipal.Profile(user.getId(), user.getEmail(),
                user.getNome(), user.getCognome(), roles, permissions),
                user.getHashedPassword(), user.getStatus(), user.getCredentialsVersion());
    }
}

