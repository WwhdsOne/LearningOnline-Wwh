package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/12 17:19
 * @description 全局异常处理
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 自定义异常捕获
     * @param e
     * @return 错误响应
     */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customExceptionHandler(XueChengPlusException e){
        log.error("系统异常:{}",e.getErrMessage());
        return new RestErrorResponse(e.getErrMessage());
    }

    /**
     * 系统异常捕获
     * @param e
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customExceptionHandler(RuntimeException e){
        log.error("系统异常:{}",e.getMessage(),e);
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    /**
     * JSR303校验异常捕获
     * @param e RestErrorResponse
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errors.add(fieldError.getDefaultMessage());
        });
        //拼接list中的错误信息
        String message = String.join(",",errors);
        log.error("系统异常:{}",message);
        return new RestErrorResponse(message);
    }
}
