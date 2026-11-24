package io.apiguardian.common;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<?> validation(MethodArgumentNotValidException ex){
  var errors=ex.getBindingResult().getFieldErrors().stream().map(e->Map.of("field",e.getField(),"message",e.getDefaultMessage()==null?"Invalid value":e.getDefaultMessage())).toList();
  return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now(),"status",400,"error","Validation failed","details",errors));
 }
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<?> status(ResponseStatusException ex){
  int code=ex.getStatusCode().value();
  return ResponseEntity.status(ex.getStatusCode()).body(Map.of("timestamp",Instant.now(),"status",code,"error",HttpStatus.valueOf(code).getReasonPhrase(),"message",ex.getReason()==null?"Request failed":ex.getReason()));
 }
}
