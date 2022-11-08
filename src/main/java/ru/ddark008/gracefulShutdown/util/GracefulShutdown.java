package ru.ddark008.gracefulShutdown.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ожидает завершения запроса перед завершением работы сервиса. Во время ожидания все новые запросы возвращают код 503
 * Работает только с методами, возвращающими ResponseEntity
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GracefulShutdown {
}
