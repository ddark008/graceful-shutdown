package ru.ddark008.gracefulShutdown.service;

import java.util.concurrent.CountDownLatch;

public interface SessionService {
    boolean createSession();

    boolean invalidateSession();

    CountDownLatch initShutdown();
}
