package com.example.messagerie.web;

import com.example.messagerie.web.dto.Dtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Dtos.ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new Dtos.ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Dtos.ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new Dtos.ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Dtos.ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new Dtos.ErrorResponse("PAYLOAD_TOO_LARGE", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Dtos.ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Dtos.ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
    }
}
