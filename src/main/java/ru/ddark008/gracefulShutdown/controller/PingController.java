package ru.ddark008.gracefulShutdown.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ddark008.gracefulShutdown.configuration.HttpSessionConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
public class PingController {
    @Autowired
    HttpSessionConfig sessionConfig;

    @GetMapping("/ping")
    public ResponseEntity ping(HttpServletRequest request) {
        if (sessionConfig.isShutdown()) {
            log.info("Ping 503");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("Unable to find session. Creating a new session");
            session = request.getSession(true);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
