package com.sparta.as.antsparticans.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class DepartmentNameAlreadyExistsAdvice {

    @ResponseBody
    @ExceptionHandler(DepartmentNameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String departmentNameAlreadyExistsHandler(DepartmentNameAlreadyExistsException e) {
        return e.getMessage();
    }
}
