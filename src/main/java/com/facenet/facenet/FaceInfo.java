package com.facenet.facenet;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Created by sh
 * 2020/3/4.
 */
public class FaceInfo {
    public Rect faceRect;
    public Point[] points;
    public float score;

    public static FaceInfo format(Box b){
        FaceInfo faceInfo=new FaceInfo();
        int[] box = b.box;
        faceInfo.faceRect=new org.opencv.core.Rect(box[0],box[1],box[2]-box[0]+1,box[3]-box[1]+1);
        faceInfo.points=b.landmark;
        faceInfo.score=b.score;
        return faceInfo;
    }
    public static List<FaceInfo> format(Vector<Box> bs){
        List<FaceInfo> list = new ArrayList<>();
        for(Box b:bs){
            list.add(format(b));
        }
        return list;
    }
}
