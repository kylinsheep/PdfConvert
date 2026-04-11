package com.demo.pdf.exceptions;

import com.demo.pdf.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(Result.fail(ex.getMessage()));
    }


    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity<Result<Void>> handleOom(OutOfMemoryError ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("Out of memory while processing PDF. Please upload a smaller file or reduce render DPI/pages."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("Internal error: " + ex.getMessage()));
    }
}
