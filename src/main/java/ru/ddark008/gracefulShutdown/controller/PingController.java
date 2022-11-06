package ru.ddark008.gracefulShutdown.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity ping(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null){
            log.info("Unable to find session. Creating a new session");
            session = request.getSession(true);
        }
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
