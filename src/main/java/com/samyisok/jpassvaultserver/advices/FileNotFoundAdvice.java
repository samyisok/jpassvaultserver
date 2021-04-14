package com.samyisok.jpassvaultserver.advices;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.samyisok.jpassvaultserver.domains.FileNotFoundException;


@ControllerAdvice
class FileNotFoundAdvice {

  @ResponseBody
  @ExceptionHandler(FileNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String fileNotFoundHandler(FileNotFoundException ex) {
    return ex.getMessage();
  }
}
