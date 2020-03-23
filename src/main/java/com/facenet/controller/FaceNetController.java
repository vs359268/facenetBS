package com.facenet.controller;

import com.facenet.bean.Result;
import com.facenet.enums.ResultEnum;
import com.facenet.service.FaceNetService;
import com.facenet.utils.ImgUtil;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sh
 * 2020/3/4.
 */
@RestController
@RequestMapping("/facenet")
public class FaceNetController {

    @Autowired
    private FaceNetService faceNetService;

    @PostMapping("/addFace")
    public Result addFace(String img, String username, String password, String name){
        if(StringUtils.isEmpty(img) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(name)){
            return Result.error(ResultEnum.PARAMETER_ERROR);
        }
        Mat mat = ImgUtil.base2Mat(img);
        if(mat==null){
            return Result.error(ResultEnum.PARAMETER_ERROR);
        }
        return faceNetService.addFace(mat,username,password,name);
    }

    @PostMapping("/recFace")
    public Result recFace(String img){
        Mat mat = ImgUtil.base2Mat(img);
        if(mat==null){
            return Result.error(ResultEnum.PARAMETER_ERROR);
        }
        return faceNetService.recFace(mat);
    }

    @PostMapping("/login")
    public Result login(String username, String password){
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return Result.error(ResultEnum.PARAMETER_ERROR);
        }
        return faceNetService.login(username,password);
    }
}
