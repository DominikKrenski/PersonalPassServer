package org.dominik.pass.advices;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.errors.api.ApiError;
import org.dominik.pass.errors.api.ValidationError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error("HANDLE METHOD ARGUMENT NOT VALID: " + Arrays.toString(ex.getStackTrace()));
    List<ValidationError> validationErrors = prepareValidationErrors(ex.getBindingResult().getFieldErrors());

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .timestamp(Instant.now())
        .message("Validation Error")
        .errors(validationErrors)
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  private List<ValidationError> prepareValidationErrors(List<FieldError> fieldErrors) {
    Map<String, ValidationError> map = new HashMap<>();

    fieldErrors
        .forEach(fieldError -> {
          if(map.containsKey(fieldError.getField())) {
            map.get(fieldError.getField()).getValidationMessages().add(fieldError.getDefaultMessage());
          } else {
            List<String> messages = new LinkedList<>();
            messages.add(fieldError.getDefaultMessage());
            map.put(fieldError.getField(),
                new ValidationError(fieldError.getField(), Objects.requireNonNull(fieldError.getRejectedValue()), messages));
          }
        });

    return new LinkedList<>(map.values());
  }
}
