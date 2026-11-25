package com.asmln.susek.api;

import com.asmln.susek.model.exception.ProjectGoneException;
import com.asmln.susek.model.exception.ProjectNotFoundException;
import com.asmln.susek.model.exception.ProjectUidAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    ProblemDetail notFound(ProjectNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProjectGoneException.class)
    ProblemDetail gone(ProjectGoneException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(ProjectUidAlreadyTakenException.class)
    ProblemDetail conflict(ProjectUidAlreadyTakenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}
