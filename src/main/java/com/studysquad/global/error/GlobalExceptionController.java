package com.studysquad.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.studysquad.global.common.FailResponse;
import com.studysquad.global.error.exception.ApiException;

@RestControllerAdvice
public class GlobalExceptionController {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<FailResponse> invalidRequestHandler(MethodArgumentNotValidException e) {
		FailResponse body = FailResponse.builder()
			.status(400)
			.message("잘못된 요청입니다")
			.build();

		for (FieldError fieldError : e.getFieldErrors()) {
			body.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
		}

		return ResponseEntity.status(400).body(body);
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<FailResponse> apiException(ApiException e) {
		int statusCode = e.getStatusCode();

		FailResponse body = FailResponse.builder()
			.status(statusCode)
			.message(e.getMessage())
			.validation(e.getValidation())
			.build();

		return ResponseEntity.status(statusCode).body(body);
	}
}