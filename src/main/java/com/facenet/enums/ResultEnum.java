package com.facenet.enums;

/**
 * Created by sh
 * 2020/3/4.
 */
public enum ResultEnum {
    UNKNOWN_ERROR(-1,"未知错误"),
    SUCCESS(0,"成功"),
    NOT_LOGIN(1,"未登录"),
    PARAMETER_ERROR(2,"参数错误"),
    LOGIN_PARAMETER_ERROR(5,"用户名或密码错误"),
    USER_NOT_FOUND(6,"用户不存在"),
    PASSWORD_ERROR(7,"密码错误"),
    USER_EXIST(10,"用户已存在"),
    NOT_FOUND(11,"未找到"),
    EXIST(12,"已存在"),

    FACE_NOT_ONE(100,"检测到多张人脸"),
    FACE_NOT_FIND(101,"未检测到人脸"),
    FACE_NOT_REC(102,"人脸库中不存在该人脸"),


    SERVER_ERROR(500,"服务器异常")
    ;

    private Integer code;
    private String msg;
    ResultEnum(Integer code,String msg){
        this.code=code;
        this.msg=msg;
    }
    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
