package ru.ddark008.gracefulShutdown.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class HttpSessionConfig {
    private final AtomicInteger activeSessions = new AtomicInteger(0);

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent hse) {
                int count = activeSessions.incrementAndGet();
                log.info("New session: {}, all {}", hse.getSession().getId(), count);
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent hse) {
                int count = activeSessions.decrementAndGet();
                log.info("Close session: {}, all {}", hse.getSession().getId(), count);
            }
        };
    }

}
