package de.toju.connectfourai.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(Exception e) {
        e.printStackTrace(); // Konsole
        return "<h1>Fehler im Spiel</h1><pre>" + e.getMessage() + "</pre>";
    }
}