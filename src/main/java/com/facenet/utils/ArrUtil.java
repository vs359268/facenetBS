package com.facenet.utils;

import org.springframework.util.StringUtils;

/**
 * Created by sh
 * 2020/3/4.
 */
public class ArrUtil {
    public static float[] formatFloat(String str,String s){
        if(StringUtils.isEmpty(str))return null;
        String[] fStr = str.split(s);
        if(fStr.length<=0)return null;
        float[] re = new float[fStr.length];
        for(int i=0;i<re.length;i++){
            re[i]=Float.valueOf(fStr[i]);
        }
        return re;
    }
    public static String floatLink(float[] arr,String s){
        if(arr==null || arr.length<0)return null;
        StringBuilder sb=new StringBuilder();
        sb.append(arr[0]);
        for(int i=1;i<arr.length;i++){
            sb.append(s);
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
