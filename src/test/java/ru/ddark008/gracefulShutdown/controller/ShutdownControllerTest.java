package ru.ddark008.gracefulShutdown.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.ddark008.gracefulShutdown.util.ParallelExecutor;

import java.util.concurrent.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
class ShutdownControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private final ParallelExecutor parallelExecutor = new ParallelExecutor();
    @Test
    void badPing() throws Exception {
        parallelExecutor.submit(getPingWithDelay(0, HttpStatus.BAD_REQUEST));
        parallelExecutor.awaitTermination(15, TimeUnit.SECONDS);
    }

    @Test
    void shutdown() throws Exception {
        parallelExecutor.submit(getPingWithDelay(500, HttpStatus.OK));
        parallelExecutor.submit(getPingWithDelay(1000, HttpStatus.OK));
        parallelExecutor.submit(getShutdownWithDelay(1000, HttpStatus.OK));
        parallelExecutor.submit(getPingWithDelay(1500, HttpStatus.BAD_GATEWAY));
        parallelExecutor.submit(getPingWithDelay(2000, HttpStatus.BAD_GATEWAY));
        parallelExecutor.submit(getPingWithDelay(3000, HttpStatus.BAD_GATEWAY));
        // Чтобы тест не завершился раньше запросов
        parallelExecutor.awaitTermination(15, TimeUnit.SECONDS);
    }

    @Test
    void shutdownOnly() throws Exception {
        parallelExecutor.submit(getShutdownWithDelay(1000, HttpStatus.OK));
        // Чтобы тест не завершился раньше запросов
        parallelExecutor.awaitTermination(15, TimeUnit.SECONDS);
    }

    private Callable<MvcResult> getPingWithDelay(long delay_ms, HttpStatus status) {
        return () -> {
            try {
                Thread.sleep(delay_ms);
                return mockMvc.perform(get("/ping"))
                        .andExpect(status().is(status.value())).andReturn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Callable<MvcResult> getShutdownWithDelay(long delay_ms, HttpStatus status) {
        return () -> {
            try {
                Thread.sleep(delay_ms);
                return mockMvc.perform(post("/shutdown"))
                        .andExpect(status().is(status.value())).andReturn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}