package ru.ddark008.gracefulShutdown.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.ddark008.gracefulShutdown.service.SessionService;

@Aspect
@Component
@Slf4j
public class GracefulShutdownAspect {

    @Autowired
    SessionService service;

    @Around("@annotation(GracefulShutdown)")
    public Object sessionWrap(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature methodSignature
                && methodSignature.getReturnType().isAssignableFrom(ResponseEntity.class)) {
            if (service.createSession()) {
                Object result = joinPoint.proceed();
                service.invalidateSession();
                return result;
            } else {
                log.info("Return 502");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }
        } else {
            return joinPoint.proceed();
        }
    }
}
