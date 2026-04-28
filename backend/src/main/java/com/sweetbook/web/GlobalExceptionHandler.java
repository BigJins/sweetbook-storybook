package com.sweetbook.web;

import com.sweetbook.web.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(f -> f.getDefaultMessage())
            .orElse("VALIDATION_FAILED");
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_FAILED", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegal(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), userMessage(e.getMessage())));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> state(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("INVALID_TRANSITION", "허용되지 않은 상태 전이입니다"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> notFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage(), userMessage(e.getMessage())));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(new ErrorResponse("FILE_TOO_LARGE", "5MB 이하 JPG/PNG만 업로드 가능합니다"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> notReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_FAILED", "요청 형식이 올바르지 않아요"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unhandled(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "서버에서 문제가 발생했어요. 잠시 후 다시 시도해주세요"));
    }

    private String userMessage(String code) {
        return switch (code) {
            case "UNSUPPORTED_IMAGE_TYPE" -> "5MB 이하 JPG/PNG만 업로드 가능합니다";
            case "DRAWING_REQUIRED" -> "그림을 업로드해주세요";
            case "STORY_NOT_COMPLETED" -> "완성된 동화로만 주문할 수 있어요";
            case "STORY_NOT_FOUND" -> "동화를 찾을 수 없어요";
            case "ORDER_NOT_FOUND" -> "주문을 찾을 수 없어요";
            case "PAGE_NOT_FOUND" -> "페이지를 찾을 수 없어요";
            default -> code;
        };
    }
}
