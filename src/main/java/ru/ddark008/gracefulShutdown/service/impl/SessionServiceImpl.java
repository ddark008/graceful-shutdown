package ru.ddark008.gracefulShutdown.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ddark008.gracefulShutdown.service.SessionService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {
    // Счетчик активных сессий в каждый момент времени
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    // Флаг для отлупа новых соедениней с 503 ошибкой
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    // Замок для синхронизации initShutdown(), чтобы не было такого, что между запросом активных соедиений и созданием CountDownLatch одна из сесий закрылась
    private final ReentrantReadWriteLock shutdownLock = new ReentrantReadWriteLock();
    // Защелка для отслеживания завершения оставшихся подключений перед завершением работы
    private CountDownLatch shutdownLatch = null;

    @Override
    public boolean createSession() {
        try {
            shutdownLock.readLock().lock();

            if (isShutdown.get()) {
                return false;
            }
            int count = activeSessions.incrementAndGet();
            log.info("New session, now is {}", count);
            return true;
        } finally {
            shutdownLock.readLock().unlock();
        }
    }

    @Override
    public boolean invalidateSession() {
        try {
            shutdownLock.readLock().lock();

            if (shutdownLatch != null) {
                shutdownLatch.countDown();
                log.info("Latch -1 now: {}", shutdownLatch.getCount());
            }
            int count = activeSessions.decrementAndGet();
            log.info("Close session, now is {}", count);
            return true;
        } finally {
            shutdownLock.readLock().unlock();
        }
    }

    @Override
    public CountDownLatch initShutdown() {
        if (shutdownLatch == null) {
            try {
                shutdownLock.writeLock().lock();
                isShutdown.set(true);
                shutdownLatch = new CountDownLatch(activeSessions.get());
                log.info("Latch create: {}", shutdownLatch.getCount());
            } finally {
                shutdownLock.writeLock().unlock();
            }
        }
        return shutdownLatch;
    }
}
