package ru.ddark008.gracefulShutdown.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ddark008.gracefulShutdown.util.GracefulShutdown;

@Slf4j
@RestController
public class PingController {

    @GetMapping("/ping")
    @GracefulShutdown
    public ResponseEntity<Object> ping() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }
}
