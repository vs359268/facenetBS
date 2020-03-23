package com.facenet.handle;


import com.facenet.bean.Result;
import com.facenet.enums.ResultEnum;
import com.facenet.exceptions.ResultException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sh
 * 2020/3/4.
 */
@ControllerAdvice
public class ExceptionHandle {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Result handle(Exception e){
        if(e instanceof ResultException){
            ResultException resultException=(ResultException)e;
            return Result.error(resultException.getCode(),resultException.getMessage());
        }else{
            e.printStackTrace();
            return Result.error(ResultEnum.SERVER_ERROR);
        }

    }
}
