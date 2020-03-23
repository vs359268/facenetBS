package com.facenet.service;

import com.facenet.bean.Result;
import org.opencv.core.Mat;

/**
 * Created by sh
 * 2020/3/5.
 */
public interface FaceNetService {
    /**
     * 添加人脸
     * @param img 人脸图片
     * @param username 用户名
     * @param password 密码
     * @param name 名字
     * @return FaceInfo：人脸框 人脸关键点
     */
    Result addFace(Mat img, String username, String password, String name);

    /**
     * 从人脸组中识别人脸
     * @param img 人脸图片
     * @return Map：关键字 名字 得分 人脸位置
     */
    Result recFace(Mat img);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果：ok
     */
    Result login(String username,String password);
}
