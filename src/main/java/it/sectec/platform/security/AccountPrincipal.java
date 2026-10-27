package it.sectec.platform.security;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AccountPrincipal extends User {
    private final Profile profile;
    private final long credentialsVersion;

    public AccountPrincipal(Profile profile, String password, String status, long credentialsVersion) {
        super(profile.email(), password, "ACTIVE".equals(status), true, true,
                !"LOCKED".equals(status),
                Stream.concat(profile.roles().stream().map(role -> "ROLE_" + role),
                        profile.permissions().stream().map(permission -> "PERM_" + permission))
                        .map(SimpleGrantedAuthority::new).toList());
        this.profile = profile;
        this.credentialsVersion = credentialsVersion;
    }

    public Profile profile() { return profile; }
    public long credentialsVersion() { return credentialsVersion; }

    public record Profile(UUID id, String email, String nome, String cognome,
                          Set<String> roles, Set<String> permissions) implements java.io.Serializable {
        public Profile {
            roles = Set.copyOf(roles);
            permissions = Set.copyOf(permissions);
        }
    }
}

