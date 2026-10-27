package it.sectec.platform.identity.service;

import it.sectec.platform.identity.entity.User;
import it.sectec.platform.identity.repository.UserRepository;
import it.sectec.platform.shared.crud.CrudException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountLifecycleService {
    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final Duration verificationTtl;
    private final Duration resetTtl;
    private final SecureRandom random = new SecureRandom();

    public AccountLifecycleService(JdbcTemplate jdbc, UserRepository users, PasswordEncoder encoder,
            ApplicationEventPublisher events, Clock clock,
            @Value("${app.account.verification-ttl:PT24H}") Duration verificationTtl,
            @Value("${app.account.reset-ttl:PT30M}") Duration resetTtl) {
        this.jdbc = jdbc; this.users = users; this.encoder = encoder; this.events = events; this.clock = clock;
        this.verificationTtl = verificationTtl; this.resetTtl = resetTtl;
    }

    public void register(String email, String password, String nome, String cognome) {
        String hash = encode(password); // Always perform hashing, including existing accounts.
        String normalized = normalize(email);
        UUID id = UUID.randomUUID();
        int inserted = jdbc.update("""
                insert into app_user(id,email,hashed_password,status,nome,cognome)
                values (?,?,?,'PENDING_VERIFICATION',?,?) on conflict do nothing
                """, id, normalized, hash, nome.trim(), cognome.trim());
        if (inserted == 1) issue(users.findLockedById(id).orElseThrow(), "VERIFY_EMAIL", verificationTtl);
        // Existing accounts are neither modified nor disclosed.
    }

    public void resendVerification(String email) {
        users.findLockedByEmail(normalize(email)).ifPresent(user -> {
            if (user.getEmailVerifiedAt() == null &&
                    ("PENDING_VERIFICATION".equals(user.getStatus()) || "ACTIVE".equals(user.getStatus())))
                issue(user, "VERIFY_EMAIL", verificationTtl);
        });
    }

    public void forgotPassword(String email) {
        users.findLockedByEmail(normalize(email)).ifPresent(user -> {
            if ("ACTIVE".equals(user.getStatus()) && user.getEmailVerifiedAt() != null)
                issue(user, "RESET_PASSWORD", resetTtl);
        });
    }

    public void verifyEmail(String token) {
        var user = consume(token, "VERIFY_EMAIL");
        if (user.getEmailVerifiedAt() != null ||
                !Set.of("ACTIVE", "PENDING_VERIFICATION").contains(user.getStatus())) invalid();
        user.setEmailVerifiedAt(clock.instant());
        if ("PENDING_VERIFICATION".equals(user.getStatus())) user.setStatus("ACTIVE");
    }

    public void resetPassword(String token, String password) {
        String hash = encode(password);
        var user = consume(token, "RESET_PASSWORD");
        if (!"ACTIVE".equals(user.getStatus()) || user.getEmailVerifiedAt() == null) invalid();
        user.setHashedPassword(hash);
        user.invalidateCredentials();
        jdbc.update("update account_token set consumed_at = ? where user_id = ? and consumed_at is null",
                Timestamp.from(clock.instant()), user.getId());
    }

    private void issue(User user, String purpose, Duration ttl) {
        Instant now = clock.instant();
        Integer recent = jdbc.queryForObject("""
                select count(*) from account_token where user_id = ? and purpose = ? and created_at > ?
                """, Integer.class, user.getId(), purpose, Timestamp.from(now.minusSeconds(60)));
        if (recent != null && recent > 0) return;
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbc.update("update account_token set consumed_at = ? where user_id = ? and purpose = ? and consumed_at is null",
                Timestamp.from(now), user.getId(), purpose);
        jdbc.update("""
                insert into account_token(id,user_id,token_hash,purpose,email,credentials_version,created_at,expires_at)
                values (?,?,?,?,?,?,?,?)
                """, UUID.randomUUID(), user.getId(), digest(token), purpose, user.getEmail(),
                user.getCredentialsVersion(), Timestamp.from(now), Timestamp.from(now.plus(ttl)));
        events.publishEvent(new AccountMail(user.getEmail(), purpose, token));
    }

    private User consume(String token, String purpose) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return invalid();
        String hash = digest(token);
        List<UUID> ids = jdbc.query("select user_id from account_token where token_hash = ? and purpose = ?",
                (rs, n) -> rs.getObject(1, UUID.class), hash, purpose);
        if (ids.isEmpty()) return invalid();
        // Every issue/consume operation locks the user first, serializing concurrent redemptions.
        var user = users.findLockedById(ids.getFirst()).orElseThrow(() -> new CrudException(400, "INVALID_TOKEN"));
        int updated = jdbc.update("""
                update account_token set consumed_at = ?
                where token_hash = ? and purpose = ? and consumed_at is null and expires_at > ?
                and email = ? and credentials_version = ?
                """, Timestamp.from(clock.instant()), hash, purpose, Timestamp.from(clock.instant()),
                user.getEmail(), user.getCredentialsVersion());
        if (updated != 1) return invalid();
        return user;
    }

    private User invalid() { throw new CrudException(400, "INVALID_TOKEN"); }
    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private String encode(String password) {
        if (password == null || password.isBlank() || password.length() < 12 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new CrudException(400, "INVALID_PASSWORD");
        return encoder.encode(password);
    }
    private String digest(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.US_ASCII))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}

