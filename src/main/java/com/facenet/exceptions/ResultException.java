package com.facenet.exceptions;


import com.facenet.enums.ResultEnum;

/**
 * Created by sh
 * 2020/3/4.
 */
public class ResultException extends RuntimeException{
    private Integer code;
    public ResultException(ResultEnum re){
        super(re.getMsg());
        this.code=re.getCode();
    }
    public ResultException(Integer code,String msg){
        super(msg);
        this.code=code;
    }
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
