package org.dominik.pass.advices;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.errors.api.ApiError;
import org.dominik.pass.errors.api.ValidationError;
import org.dominik.pass.errors.exceptions.BaseException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
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

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error("HANDLE TYPE MISMATCH: " + Arrays.toString(ex.getStackTrace()));

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.BAD_REQUEST)
        .timestamp(Instant.now())
        .message("Bad Path Variable")
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error("HANDLE HTTP MESSAGE NOT READABLE: " + Arrays.toString(ex.getStackTrace()));

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.BAD_REQUEST)
        .timestamp(Instant.now())
        .message("Message is not formatted properly")
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error("HANDLE METHOD NOT SUPPORTED: " + Arrays.toString(ex.getStackTrace()));

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .timestamp(Instant.now())
        .message("Method not allowed for given route")
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error("HANDLE HANDLER NOT FOUND: " + Arrays.toString(ex.getStackTrace()));
    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.BAD_REQUEST)
        .timestamp(Instant.now())
        .message("Given route does not exist")
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }



  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error("HANDLE MEDIA TYPE NOT SUPPORTED: " + Arrays.toString(ex.getStackTrace()));
    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .timestamp(Instant.now())
        .message("MediaType Not Supported")
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
    log.error("HANDLE CONSTRAINT VIOLATION EXCEPTION: " + Arrays.toString(ex.getStackTrace()));

    List<ValidationError> validationErrors = prepareValidationErrors(ex.getConstraintViolations());

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .timestamp(Instant.now())
        .message("Validation Error")
        .errors(validationErrors)
        .build();

    return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<Object> handleBaseException(BaseException ex) {
    log.error("HANDLE BASE EXCEPTION:" + Arrays.toString(ex.getStackTrace()));
    ApiError apiError = ApiError
        .builder()
        .status(ex.getStatus())
        .timestamp(ex.getTimestamp())
        .message(ex.getMessage())
        .build();

    return new ResponseEntity<>(apiError, ex.getStatus());
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
                new ValidationError(fieldError.getField(), fieldError.getRejectedValue(), messages));
          }
        });

    return new LinkedList<>(map.values());
  }

  private List<ValidationError> prepareValidationErrors(Set<ConstraintViolation<?>> violations) {
    Map<String, ValidationError> map = new HashMap<>();

    violations
        .forEach(violation -> {
          String fieldName = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();

          if (map.containsKey(fieldName)) {
            map.get(fieldName).getValidationMessages().add(violation.getMessage());
          } else {
            List<String> messages = new LinkedList<>();
            messages.add(violation.getMessage());
            map.put(fieldName, new ValidationError(fieldName, violation.getInvalidValue(), messages));
          }
        });

    return new LinkedList<>(map.values());
  }
}
