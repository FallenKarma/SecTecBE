package it.sectec.platform.config;

import java.time.Clock;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@EnableAsync
public class AccountConfig {
    @Bean Clock accountClock() { return Clock.systemUTC(); }
    @Bean("accountMailExecutor")
    Executor accountMailExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("account-mail-");
        executor.initialize();
        return executor;
    }
}

