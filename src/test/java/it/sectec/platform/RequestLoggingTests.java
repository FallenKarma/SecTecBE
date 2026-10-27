package it.sectec.platform;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.sectec.platform.shared.RequestLoggingFilter;
import it.sectec.platform.shared.TransactionLog;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.HandlerMapping;
import static org.assertj.core.api.Assertions.*;

class RequestLoggingTests {
    @Test
    void requestIdIsGeneratedAndRestoredWithoutLoggingUntrustedInput() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        Level previousLevel = logger.getLevel();
        var logs = new ListAppender<ILoggingEvent>();
        logs.start();
        logger.addAppender(logs);
        logger.setLevel(Level.DEBUG);
        MDC.put("requestId", "outer-context");
        try {
            var request = new MockHttpServletRequest("POST", "/secret-path");
            request.setQueryString("token=secret-token");
            request.setContent("secret-password".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            request.addHeader("X-Request-ID", "untrusted-id");
            var response = new MockHttpServletResponse();
            new RequestLoggingFilter().doFilter(request, response, (req, res) -> {
                assertThat(MDC.get("requestId")).isEqualTo(response.getHeader("X-Request-ID"))
                        .isNotEqualTo("untrusted-id").isNotEqualTo("outer-context");
                req.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/auth/verify-email");
                response.setStatus(204);
            });
            assertThat(response.getHeader("X-Request-ID")).matches("[a-f0-9-]{36}");
            assertThat(MDC.get("requestId")).isEqualTo("outer-context");
            String output = logs.list.toString();
            assertThat(output).contains("status=204", "/api/v1/auth/verify-email")
                    .doesNotContain("secret-path", "secret-token", "secret-password", "untrusted-id");
        } finally {
            MDC.clear();
            logger.detachAppender(logs);
            logger.setLevel(previousLevel);
            logs.stop();
        }
    }

    @Test
    void failureIsRethrownWithoutLoggingExceptionMessageOrLeakingMdc() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        var logs = new ListAppender<ILoggingEvent>();
        logs.start();
        logger.addAppender(logs);
        try {
            var failure = new IllegalStateException("secret-database-value");
            assertThatThrownBy(() -> new RequestLoggingFilter().doFilter(
                    new MockHttpServletRequest("POST", "/secret"), new MockHttpServletResponse(),
                    (req, res) -> { throw failure; })).isSameAs(failure);
            assertThat(MDC.get("requestId")).isNull();
            assertThat(logs.list.toString()).contains("IllegalStateException", "status=500")
                    .doesNotContain("secret-database-value");
            assertThat(logs.list).allSatisfy(event -> assertThat(event.getThrowableProxy()).isNull());
        } finally {
            logger.detachAppender(logs);
            logs.stop();
        }
    }

    @Test
    void successIsLoggedOnlyAfterCommitAndNeverOnRollback() {
        Logger logger = (Logger) LoggerFactory.getLogger("it.sectec.platform.transaction-test");
        var logs = new ListAppender<ILoggingEvent>();
        logs.start();
        logger.addAppender(logs);
        TransactionSynchronizationManager.initSynchronization();
        try {
            TransactionLog.committed(logger, "successful-write");
            assertThat(logs.list).isEmpty();
            var callback = TransactionSynchronizationManager.getSynchronizations().getFirst();
            callback.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            assertThat(logs.list).noneMatch(event -> event.getLevel() == Level.INFO);
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.initSynchronization();
            TransactionLog.committed(logger, "successful-write");
            TransactionSynchronizationManager.getSynchronizations().getFirst().afterCommit();
            assertThat(logs.list).anyMatch(event -> event.getLevel() == Level.INFO
                    && event.getFormattedMessage().equals("successful-write"));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            logger.detachAppender(logs);
            logs.stop();
        }
    }
}
