package com.adl.genius.handler;

import com.adl.genius.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    public <T> Response<T> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Response.error(e.getMessage());
    }
}
