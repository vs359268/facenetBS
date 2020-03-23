package com.facenet.bean;


import com.facenet.enums.ResultEnum;
import lombok.Data;

/**
 *
 * Created by sh
 * 2020/3/4.
 */
@Data
public class Result<T> {
    //状态码
    private Integer code;
    //提示信息
    private String msg;
    //内容
    private T data;

    public static Result success(Object t){
        Result re=new Result();
        re.setMsg("成功");
        re.setCode(0);
        re.setData(t);
        return re;
    }
    public static Result success(){
        return success(null);
    }
    public static Result error(Integer code,String msg){
        Result re=new Result();
        re.setMsg(msg);
        re.setCode(code);
        return re;
    }

    public static Result error(ResultEnum resultEnum) {
        return error(resultEnum.getCode(),resultEnum.getMsg());
    }
}
