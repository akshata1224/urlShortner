package com.example.urlShortner.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UrlNotFoundException.class)
	public ProblemDetail handleNotFound(UrlNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("URL Not Found");
		problem.setProperty("code", ex.getCode());
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(UrlExpiredException.class)
	public ProblemDetail handleExpired(UrlExpiredException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
		problem.setTitle("URL Expired");
		problem.setProperty("code", ex.getCode());
		problem.setProperty("expiresAt", ex.getExpiresAt());
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(AliasAlreadyExistsException.class)
	public ProblemDetail handleAliasConflict(AliasAlreadyExistsException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Alias Already Exists");
		problem.setProperty("alias", ex.getAlias());
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(ReservedAliasException.class)
	public ProblemDetail handleReservedAlias(ReservedAliasException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Reserved Alias");
		problem.setProperty("alias", ex.getAlias());
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(InvalidAliasException.class)
	public ProblemDetail handleInvalidAlias(InvalidAliasException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Invalid Alias");
		problem.setProperty("alias", ex.getAlias());
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(InvalidTtlException.class)
	public ProblemDetail handleInvalidTtl(InvalidTtlException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Invalid TTL");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(ShortCodeGenerationException.class)
	public ProblemDetail handleGenerationFailure(ShortCodeGenerationException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
		problem.setTitle("Short Code Generation Failed");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}

		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problem.setTitle("Invalid Request");
		problem.setProperty("errors", errors);
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problem.setTitle("Invalid Request");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Invalid Path");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unhandled exception", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred"
		);
		problem.setTitle("Internal Server Error");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}
}
