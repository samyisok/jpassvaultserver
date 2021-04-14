package com.samyisok.jpassvaultserver.advices;


import com.samyisok.jpassvaultserver.domains.FileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
class AuthTokenAdvice {

  @ResponseBody
  @ExceptionHandler(FileNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String AuthTokenHandler(FileNotFoundException ex) {
    return ex.getMessage();
  }
}
