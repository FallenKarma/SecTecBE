package it.sectec.platform.identity.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AccountMailSender {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AccountMailSender.class);
    private final JavaMailSender mail;
    private final String from;
    private final String baseUrl;
    public AccountMailSender(JavaMailSender mail, @Value("${app.account.mail-from}") String from,
                             @Value("${app.account.frontend-base-url}") String baseUrl) {
        this.mail = mail;
        this.from = from;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @Async("accountMailExecutor")
    @TransactionalEventListener
    public void send(AccountMail event) {
        boolean verification = event.purpose().equals("VERIFY_EMAIL");
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.email());
        message.setSubject(verification ? "SecTec - verifica email" : "SecTec - reimposta password");
        // Fragment is not sent in HTTP requests or Referer headers.
        String action = verification ? "verify-email" : "reset-password";
        message.setText("Apri il link e conferma l'operazione:\n" + baseUrl +
                "/#" + action + "?token=" + event.token() +
                "\n\nIl link scade ed e' utilizzabile una sola volta. Se non hai richiesto l'operazione, ignora questa email.");
        try {
            log.debug("Account email delivery started: purpose={}", event.purpose());
            mail.send(message);
            log.info("Account email accepted by SMTP server: purpose={}", event.purpose());
        } catch (MailException ex) {
            // Never log credentials, token, message body or SMTP exception details.
            log.error("Account email delivery failed: purpose={} exceptionType={}; check SMTP availability/configuration and request a new link after 60 seconds",
                    event.purpose(), ex.getClass().getSimpleName());
        }
    }
}

