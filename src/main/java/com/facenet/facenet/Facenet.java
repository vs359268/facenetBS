package com.facenet.facenet;


import com.facenet.utils.FilePath;
import com.facenet.utils.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**功能：人脸转换为512维特征向量
 */
public class Facenet{
    private String MODEL_FILE  = FilePath.getResource("data/facenet_model_2020-03-08.pb");
    private static final String INPUT_NAME  = "input:0";
    private static final String OUTPUT_NAME = "embeddings:0";
    private static final String PHASE_NAME  = "phase_train:0";
    //输入图片大小.(图片非此大小，会rescale)
    private static final int INPUT_SIZE=160;
    private float[] floatValues;  //保存input的值
    
    private Session session;

    private static Facenet facenet=new Facenet();
    
    public Facenet(){
        loadModel();
        floatValues=new float[INPUT_SIZE*INPUT_SIZE*3];
    }
    private void loadModel(){
        try {
            Graph graph = new Graph();
            byte[] graphBytes = IOUtils.readFileByBytes(MODEL_FILE);
            graph.importGraphDef(graphBytes);
            session = new Session(graph);
        }catch(Exception e){
            System.out.println("[*]load model failed"+e);
        }
    }
    private void normalizeImage(Mat img){
        Mat bitmap=new Mat();
        Imgproc.resize(img,bitmap,new Size(160,160));

        int width = bitmap.cols();
        int height = bitmap.rows();
        int dims = bitmap.channels();

        float imageMean=127.5f;
        float imageStd=128;

        byte[] rgbData = new byte[width*height*dims];
        bitmap.get(0, 0, rgbData);
        for (int i=0;i<rgbData.length;i++){
            floatValues[i] = ((rgbData[i] & 0xFF) - imageMean) / imageStd;
        }
    }
    public FaceFeature recognizeImage(Mat bitmap){
        normalizeImage(bitmap);
        try {
            byte[] b = new byte[]{0};
            Tensor<?> out = session.runner()
                    .feed(INPUT_NAME, Tensor.create(new long[]{1, INPUT_SIZE, INPUT_SIZE, 3}, FloatBuffer.wrap(floatValues)))
                    .feed(PHASE_NAME,Tensor.create(Boolean.class, new long[]{}, ByteBuffer.wrap(b)))
                    .fetch(OUTPUT_NAME).run().get(0);
            FaceFeature faceFeature=new FaceFeature();
            float[] outputs=faceFeature.getFeature();
            out.writeTo(FloatBuffer.wrap(outputs));
            return faceFeature;
        }catch (Exception e){
            System.out.println("[*] feed Error\n"+e);
            return null;
        }
    }

    public static FaceFeature get(Mat bitmap){
        return facenet.recognizeImage(bitmap);
    }
}
