package ru.ddark008.gracefulShutdown.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
class ShutdownControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Test
    void shutdown() throws Exception {
        executorService.submit(getPingWithDelay(0, HttpStatus.OK));
        executorService.submit(getPingWithDelay(500, HttpStatus.OK));
        executorService.submit(getPingWithDelay(1000, HttpStatus.OK));
        executorService.submit(getShutdownWithDelay(1000, HttpStatus.OK));
        executorService.submit(getPingWithDelay(1500, HttpStatus.BAD_GATEWAY));
        executorService.submit(getPingWithDelay(2000, HttpStatus.BAD_GATEWAY));
        executorService.submit(getPingWithDelay(3000, HttpStatus.BAD_GATEWAY));
        // Чтобы тест не завершился раньше запросов
        executorService.awaitTermination(15, TimeUnit.SECONDS);
    }

    @Test
    void shutdownOnly() throws Exception {
        executorService.submit(getShutdownWithDelay(1000, HttpStatus.OK));
        // Чтобы тест не завершился раньше запросов
        executorService.awaitTermination(15, TimeUnit.SECONDS);
    }

    private Runnable getPingWithDelay(long delay_ms, HttpStatus status) {
        return () -> {
            try {
                Thread.sleep(delay_ms);
                mockMvc.perform(get("/ping"))
                        .andExpect(status().is(status.value()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable getShutdownWithDelay(long delay_ms, HttpStatus status) {
        return () -> {
            try {
                Thread.sleep(delay_ms);
                mockMvc.perform(post("/shutdown"))
                        .andExpect(status().is(status.value()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}