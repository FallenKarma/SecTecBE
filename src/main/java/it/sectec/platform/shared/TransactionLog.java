package it.sectec.platform.shared;

import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Never report a successful write before its transaction has committed. */
public final class TransactionLog {
    private TransactionLog() {}

    public static void committed(Logger logger, String message) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            logger.info(message);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { logger.info(message); }
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) logger.debug("Transaction did not commit: operation={}", message);
            }
        });
    }
}
