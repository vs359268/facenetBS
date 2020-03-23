package com.facenet.facenet;
/**
 * 人脸特征(512维特征值)
 * 相似度取特征向量之间的欧式距离.
 */
public class FaceFeature {
    public static final int DIMS=512;
    private float fea[];
    FaceFeature(){
        fea=new float[DIMS];
    }
    public float[] getFeature(){
        return fea;
    }
    //比较当前特征和另一个特征之间的相似度
    public double compare(FaceFeature ff){
        return compare(ff.fea);
    }
    public double compare(float[] data){
        return compare(fea,data);
    }
    public static double compare(float[] data1,float[] data2){
        double dist=0;
        for (int i=0;i<DIMS;i++)
            dist+=(data1[i]-data2[i])*(data1[i]-data2[i]);
        dist=Math.sqrt(dist);
        return dist;
    }
}
