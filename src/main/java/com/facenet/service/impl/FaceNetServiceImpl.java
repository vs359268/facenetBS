package com.facenet.service.impl;

import com.facenet.bean.FaceData;
import com.facenet.bean.Result;
import com.facenet.enums.ResultEnum;
import com.facenet.exceptions.ResultException;
import com.facenet.facenet.*;
import com.facenet.repository.FaceDataRepository;
import com.facenet.service.FaceNetService;
import com.facenet.utils.ArrUtil;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by yl
 * 2020/3/4.
 */
@Service
public class FaceNetServiceImpl implements FaceNetService{
    private static final int MIN_SIZE=50;//人脸最小尺寸（像素）
    private static final double MIN_SCORE=0.9;//人脸识别阈值 小于0.9即识别成功

    @Autowired
    private FaceDataRepository faceDataRepository;

    /**
     * 添加人脸
     * @param img   人脸图片
     * @param username 分组
     * @param password   关键字
     * @param name  名字
     * @return FaceInfo：人脸框 人脸关键点
     */
    @Override
    public Result addFace(Mat img, String username, String password, String name) {
        FaceData fd = faceDataRepository.findFirstByUsername(username);//根据用户名查询数据
        if(fd!=null){
            throw new ResultException(ResultEnum.EXIST);//用户名已存在
        }
        List<FaceInfo> faces = FaceInfo.format(MTCNN.find(img,MIN_SIZE));//获得所有人脸位置
        if(faces==null || faces.size()<=0){//人脸位置为空
            throw new ResultException(ResultEnum.FACE_NOT_FIND);//人脸未找到异常
        }
        if(faces.size()>1){//人脸数量大于1
            throw new ResultException(ResultEnum.FACE_NOT_ONE);//人脸不唯一异常
        }
        Mat face = img.submat(faces.get(0).faceRect);//取第一个人脸位置（这里有且仅有一个），根据人脸框剪裁出人脸
        FaceFeature faceFeature = Facenet.get(face);//计算出特征向量
        FaceData faceData=new FaceData();//构造保存数据
        faceData.setUsername(username);
        faceData.setPassword(password);
        faceData.setName(name);
        faceData.setData(ArrUtil.floatLink(faceFeature.getFeature(),","));//将人脸特征向量转为字符串存储
        faceDataRepository.save(faceData);//JPA保存数据库
        return Result.success(faces.get(0));
    }

    /**
     * 从人脸组中识别人脸
     * @param img   人脸图片
     * @return Map：关键字 名字 得分 人脸位置
     */
    @Override
    public Result recFace(Mat img) {
        List<FaceInfo> faces = FaceInfo.format(MTCNN.find(img,MIN_SIZE));//找出所有人脸位置
        if(faces==null || faces.size()<=0){//人脸位置为空
            throw new ResultException(ResultEnum.FACE_NOT_FIND);//人脸未找到异常
        }
        if(faces.size()>1){//人脸数量大于1
            throw new ResultException(ResultEnum.FACE_NOT_ONE);//人脸不唯一异常
        }
        Mat face = img.submat(faces.get(0).faceRect);//取第一个人脸位置（这里有且仅有一个），根据人脸框剪裁出人脸
        FaceFeature faceFeature = Facenet.get(face);//计算出特征向量
        List<FaceData> faceDatas= faceDataRepository.findAll();//获得数据库中的所有人脸数据
        Map<String,Object> map = recFaceOne(faceDatas,faceFeature);//循环比较人脸数据，得到识别人脸
        map.put("rect",faces.get(0).faceRect);//返回参数加入人脸位置框
        return Result.success(map);
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果：ok
     */
    @Override
    public Result login(String username, String password) {
        FaceData faceData = faceDataRepository.findFirstByUsernameAndPassword(username, password);
        if(faceData == null){
            throw new ResultException(ResultEnum.LOGIN_PARAMETER_ERROR);
        }
        return Result.success("OK");
    }


    private Map<String,Object> recFaceOne(List<FaceData> faceDatas,FaceFeature faceFeature){
        double minScore=MIN_SCORE;
        FaceData faceData=null;
        for(FaceData fd:faceDatas){//遍历所有人脸数据
            float[] data = ArrUtil.formatFloat(fd.getData(),",");//数据库数据转换为向量
            if(data==null || data.length!=FaceFeature.DIMS){
                continue;
            }
            double score = faceFeature.compare(data);//比较
            if(score<minScore){//取最小分数
                minScore=score;
                faceData=fd;
            }
        }
        if(faceData==null){//未找到，即整个人脸集合的得分没有小于minScore的人脸
            throw new ResultException(ResultEnum.FACE_NOT_REC);
        }
        Map<String,Object> map= new HashMap<>();//构造返回数据
        map.put("username",faceData.getUsername());
        map.put("name",faceData.getName());
        map.put("score",minScore);
        return map;
    }
}
