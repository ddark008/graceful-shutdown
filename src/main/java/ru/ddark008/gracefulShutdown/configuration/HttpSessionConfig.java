package ru.ddark008.gracefulShutdown.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Configuration
public class HttpSessionConfig {
    // Счетчик активных сессий в каждый момент времени
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    // Флаг для отлупа новых соедениней с 503 ошибкой
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    // Защелка для отслеживания завершения оставшихся подключений перед завершением работы
    private CountDownLatch shutdownLatch = null;
    // Замок для синхронизации initShutdown(), чтобы не было такого, что между запросом активных соедиений и созданием CountDownLatch одна из сесий закрылась
    private final ReentrantReadWriteLock shutdownLock = new ReentrantReadWriteLock();

    public CountDownLatch initShutdown() {
        try {
            shutdownLock.writeLock().lock();
            if (shutdownLatch == null) {
                isShutdown.set(true);
                shutdownLatch = new CountDownLatch(activeSessions.get());
                log.info("Latch create: {}", shutdownLatch.getCount());
            }
        } finally {
            shutdownLock.writeLock().unlock();
        }
        return shutdownLatch;
    }

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent hse) {
                try {
                    shutdownLock.readLock().lock();
                    int count = activeSessions.incrementAndGet();
                    log.info("New session: {}, now is {}", hse.getSession().getId(), count);
                } finally {
                    shutdownLock.readLock().unlock();
                }
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent hse) {
                try {
                    shutdownLock.readLock().lock();
                    if (shutdownLatch != null) {
                        shutdownLatch.countDown();
                        log.info("Latch -1 now: {}", shutdownLatch.getCount());
                    }
                    int count = activeSessions.decrementAndGet();
                    log.info("Close session: {}, all {}", hse.getSession().getId(), count);
                } finally {
                    shutdownLock.readLock().unlock();
                }
            }
        };
    }

    public boolean isShutdown() {
        return isShutdown.get();
    }
}
