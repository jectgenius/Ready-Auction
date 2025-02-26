package com.readyauction.app.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMissingParams(MissingServletRequestParameterException ex) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("error/404"); // 404 오류 페이지
        mav.addObject("message", "Required parameter is missing: " + ex.getParameterName());
        return mav;
    }
}
