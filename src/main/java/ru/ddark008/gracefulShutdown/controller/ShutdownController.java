package ru.ddark008.gracefulShutdown.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ddark008.gracefulShutdown.configuration.HttpSessionConfig;

@Slf4j
@RestController
public class ShutdownController {
    @Autowired
    HttpSessionConfig sessionConfig;

    @PostMapping("/shutdown")
    public ResponseEntity<Object> shutdown(){
        log.info("Shutdown start");
        try {
            sessionConfig.initShutdown().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Shutdown finish");
        return ResponseEntity.ok().build();
    }
}
