package com.example.find_campus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.find_campus.dto.ApiResponseDto;

@RestControllerAdvice(assignableTypes = {
        UserApiController.class,
        ItemApiController.class,
        CommonApiController.class,
        NotificationApiController.class,
        MatchApiController.class
})
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleServerError(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(new ApiResponseDto<>(false, "서버 오류가 발생했습니다.", null));
    }
}
