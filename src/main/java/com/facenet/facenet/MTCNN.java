package com.facenet.facenet;

import com.facenet.utils.FilePath;
import com.facenet.utils.IOUtils;
import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MTCNN {
    //参数
    private float factor=0.709f;
    private float PNetThreshold=0.6f;
    private float RNetThreshold=0.7f;
    private float ONetThreshold=0.7f;
    //MODEL PATH
    private static final String MODEL_FILE  = FilePath.getResource("data/mtcnn_model-2020-0308.pb");
    //tensor name
    private static final String   PNetInName  ="pnet/input:0";
    private static final String[] PNetOutName =new String[]{ "pnet/prob1:0","pnet/conv4-2/BiasAdd:0"};
    private static final String   RNetInName  ="rnet/input:0";
    private static final String[] RNetOutName =new String[]{ "rnet/prob1:0","rnet/conv5-2/conv5-2:0",};
    private static final String   ONetInName  ="onet/input:0";
    private static final String[] ONetOutName =new String[]{ "onet/prob1:0","onet/conv6-2/conv6-2:0","onet/conv6-3/conv6-3:0"};

    private long lastProcessTime;   //最后一张图片处理的时间ms

    private Session session;

    private static MTCNN mtcnn=new MTCNN();

    private MTCNN(){
        loadModel();
    }
    private void loadModel() {
        try {
            Graph graph = new Graph();
            byte[] graphBytes = IOUtils.readFileByBytes(MODEL_FILE);
            System.out.println(graphBytes.length);
            graph.importGraphDef(graphBytes);
            session = new Session(graph);
        }catch(Exception e){
            System.out.println("[*]load model failed"+e);
        }
    }
    //读取Bitmap像素值，预处理(-127.5 /128)，转化为一维数组返回
    private float[] normalizeImage(Mat img){

        int width = img.cols();
        int height = img.rows();
        int dims = img.channels();
        float[] floatValues=new float[width*height*dims];

        float imageMean=127.5f;
        float imageStd=128;

        byte[] rgbData = new byte[width*height*dims];
        img.get(0, 0, rgbData);
        for (int i=0;i<rgbData.length;i++){
            floatValues[i] = ((rgbData[i] & 0xFF) - imageMean) / imageStd;
        }
        return floatValues;
    }

    public Object[] parseOutName(String name) {
        Object[] tid = new Object[2];
        int colonIndex = name.lastIndexOf(58);
        if (colonIndex < 0) {
            tid[0] = 0;
            tid[1] = name;
        } else {
            try {
                tid[0] = Integer.parseInt(name.substring(colonIndex + 1));
                tid[1] = name.substring(0, colonIndex);
            } catch (NumberFormatException var4) {
                tid[0] = 0;
                tid[1] = name;
            }

        }
        return tid;
    }

    //输入前要翻转，输出也要翻转
    private  int PNetForward(Mat bitmap,float [][]PNetOutProb,float[][][]PNetOutBias){
        int w=bitmap.cols();
        int h=bitmap.rows();

        float[] PNetIn=normalizeImage(bitmap);
        flip_diag(PNetIn,h,w,3); //沿着对角线翻转

        int PNetOutSizeW=(int)Math.ceil(w*0.5-5);
        int PNetOutSizeH=(int)Math.ceil(h*0.5-5);
        float[] PNetOutP=new float[PNetOutSizeW*PNetOutSizeH*2];
        float[] PNetOutB=new float[PNetOutSizeW*PNetOutSizeH*4];

        Object[] outName1=parseOutName(PNetOutName[0]);
        Object[] outName2=parseOutName(PNetOutName[1]);

        List<Tensor<?>> outs = session.runner()
                .feed(PNetInName,Tensor.create(new long[]{1,w,h,3}, FloatBuffer.wrap(PNetIn)))
                .fetch((String)outName1[1],(Integer)outName1[0])
                .fetch((String)outName2[1],(Integer)outName2[0])
                .run();
        Tensor<?> out=outs.get(0);
        out.writeTo(FloatBuffer.wrap(PNetOutP));
        out=outs.get(1);
        out.writeTo(FloatBuffer.wrap(PNetOutB));
        //【写法一】先翻转，后转为2/3维数组
//        Utils.flip_diag(PNetOutP,PNetOutSizeW,PNetOutSizeH,2);
//        Utils.flip_diag(PNetOutB,PNetOutSizeW,PNetOutSizeH,4);
//        Utils.expand(PNetOutB,PNetOutBias);
//        Utils.expandProb(PNetOutP,PNetOutProb);

        //【写法二】这个比较快，快了3ms。意义不大，用上面的方法比较直观
        for (int y=0;y<PNetOutSizeH;y++)
            for (int x=0;x<PNetOutSizeW;x++){
               int idx=PNetOutSizeH*x+y;
               PNetOutProb[y][x]=PNetOutP[idx*2+1];
               for(int i=0;i<4;i++)
                   PNetOutBias[y][x][i]=PNetOutB[idx*4+i];
            }

        return 0;
    }
    //Non-Maximum Suppression
    //nms，不符合条件的deleted设置为true
    private void nms(Vector<Box> boxes,float threshold,String method){
        //NMS.两两比对
        //int delete_cnt=0;
        for(int i=0;i<boxes.size();i++) {
            Box box = boxes.get(i);
            if (!box.deleted) {
                //score<0表示当前矩形框被删除
                for (int j = i + 1; j < boxes.size(); j++) {
                    Box box2=boxes.get(j);
                    if (!box2.deleted) {
                        int x1 = max(box.box[0], box2.box[0]);
                        int y1 = max(box.box[1], box2.box[1]);
                        int x2 = min(box.box[2], box2.box[2]);
                        int y2 = min(box.box[3], box2.box[3]);
                        if (x2 < x1 || y2 < y1) continue;
                        int areaIoU = (x2 - x1 + 1) * (y2 - y1 + 1);
                        float iou=0f;
                        if (method.equals("Union"))
                            iou = 1.0f*areaIoU / (box.area() + box2.area() - areaIoU);
                        else if (method.equals("Min"))
                            iou= 1.0f*areaIoU / (min(box.area(),box2.area()));
                        if (iou >= threshold) { //删除prob小的那个框
                            if (box.score>box2.score)
                                box2.deleted=true;
                            else
                                box.deleted=true;
                            //delete_cnt++;
                        }
                    }
                }
            }
        }
        //Log.i(TAG,"[*]sum:"+boxes.size()+" delete:"+delete_cnt);
    }
    private int generateBoxes(float[][] prob,float[][][]bias,float scale,float threshold,Vector<Box> boxes){
        int h=prob.length;
        int w=prob[0].length;
        //Log.i(TAG,"[*]height:"+prob.length+" width:"+prob[0].length);
        for (int y=0;y<h;y++)
            for (int x=0;x<w;x++){
                float score=prob[y][x];
                //only accept prob >threadshold(0.6 here)
                if (score>PNetThreshold){
                    Box box=new Box();
                    //score
                    box.score=score;
                    //box
                    box.box[0]=Math.round(x*2/scale);
                    box.box[1]=Math.round(y*2/scale);
                    box.box[2]=Math.round((x*2+11)/scale);
                    box.box[3]=Math.round((y*2+11)/scale);
                    //bbr
                    for(int i=0;i<4;i++)
                        box.bbr[i]=bias[y][x][i];
                    //add
                    boxes.addElement(box);
                }
            }
        return 0;
    }
    private void BoundingBoxReggression(Vector<Box> boxes){
        for (int i=0;i<boxes.size();i++)
            boxes.get(i).calibrate();
    }
    //Pnet + Bounding Box Regression + Non-Maximum Regression
    /* NMS执行完后，才执行Regression
     * (1) For each scale , use NMS with threshold=0.5
     * (2) For all candidates , use NMS with threshold=0.7
     * (3) Calibrate Bounding Box
     * 注意：CNN输入图片最上面一行，坐标为[0..width,0]。所以Bitmap需要对折后再跑网络;网络输出同理.
     */
    private Vector<Box> PNet(Mat  bitmap,int minSize){
        int whMin=min(bitmap.rows(),bitmap.cols());
        float currentFaceSize=minSize;  //currentFaceSize=minSize/(factor^k) k=0,1,2... until excced whMin
        Vector<Box> totalBoxes=new Vector<Box>();
        //【1】Image Paramid and Feed to Pnet
        while (currentFaceSize<=whMin){
            float scale=12.0f/currentFaceSize;
            //(1)Image Resize
            Mat bm=new Mat();
            int changedH = (int) Math.ceil(bitmap.rows() * scale);
            int changedW = (int) Math.ceil(bitmap.cols() * scale);
            Imgproc.resize(bitmap,bm,new Size(changedW,changedH));
            int w=bm.cols();
            int h=bm.rows();
            //(2)RUN CNN
            int PNetOutSizeW=(int)(Math.ceil(w*0.5-5)+0.5);
            int PNetOutSizeH=(int)(Math.ceil(h*0.5-5)+0.5);
            float[][]   PNetOutProb=new float[PNetOutSizeH][PNetOutSizeW];
            float[][][] PNetOutBias=new float[PNetOutSizeH][PNetOutSizeW][4];
            PNetForward(bm,PNetOutProb,PNetOutBias);
            //(3)数据解析
            Vector<Box> curBoxes=new Vector<Box>();
            generateBoxes(PNetOutProb,PNetOutBias,scale,PNetThreshold,curBoxes);
            //Log.i(TAG,"[*]CNN Output Box number:"+curBoxes.size()+" Scale:"+scale);
            //(4)nms 0.5
            nms(curBoxes,0.5f,"Union");
            //(5)add to totalBoxes
            for (int i=0;i<curBoxes.size();i++)
                if (!curBoxes.get(i).deleted)
                    totalBoxes.addElement(curBoxes.get(i));
            //Face Size等比递增
            currentFaceSize/=factor;
        }
        //NMS 0.7
        nms(totalBoxes,0.7f,"Union");
        //BBR
        BoundingBoxReggression(totalBoxes);
        return updateBoxes(totalBoxes);
    }
    //截取box中指定的矩形框(越界要处理)，并resize到size*size大小，返回数据存放到data中。
    private void crop_and_resize(Mat bitmap,Box box,int size,float[] data){
        Rect rect=new Rect(box.left(),box.top(),box.width(), box.height());
        Mat face = new Mat(bitmap,rect);

        Mat croped=new Mat();
        Imgproc.resize(face,croped,new Size(size,size));

        byte[] rgbData = new byte[size*size*3];
        float imageMean=127.5f;
        float imageStd=128;
        croped.get(0, 0, rgbData);
        for (int i=0;i<rgbData.length;i++){
            data[i] = ((rgbData[i] & 0xFF) - imageMean) / imageStd;
        }
    }
    /*
     * RNET跑神经网络，将score和bias写入boxes
     */
    private void RNetForward(float[] RNetIn,Vector<Box>boxes){
        int num=RNetIn.length/24/24/3;
        //fetch
        float[] RNetP=new float[num*2];
        float[] RNetB=new float[num*4];

        Object[] outName1=parseOutName(RNetOutName[0]);
        Object[] outName2=parseOutName(RNetOutName[1]);

        List<Tensor<?>> outs = session.runner()
                .feed(RNetInName,Tensor.create(new long[]{num,24,24,3}, FloatBuffer.wrap(RNetIn)))
                .fetch((String)outName1[1],(Integer)outName1[0])
                .fetch((String)outName2[1],(Integer)outName2[0])
                .run();
        Tensor<?> out=outs.get(0);
        out.writeTo(FloatBuffer.wrap(RNetP));
        out=outs.get(1);
        out.writeTo(FloatBuffer.wrap(RNetB));

        //转换
        for (int i=0;i<num;i++) {
            boxes.get(i).score = RNetP[i * 2 + 1];
            for (int j=0;j<4;j++)
                boxes.get(i).bbr[j]=RNetB[i*4+j];
        }
    }
    //Refine Net
    private Vector<Box> RNet(Mat bitmap,Vector<Box> boxes){
        //RNet Input Init
        int num=boxes.size();
        float[] RNetIn=new float[num*24*24*3];
        float[] curCrop=new float[24*24*3];
        int RNetInIdx=0;
        for (int i=0;i<num;i++){
            crop_and_resize(bitmap,boxes.get(i),24,curCrop);
            flip_diag(curCrop,24,24,3);
            //Log.i(TAG,"[*]Pixels values:"+curCrop[0]+" "+curCrop[1]);
            for (int j=0;j<curCrop.length;j++) RNetIn[RNetInIdx++]= curCrop[j];
        }
        //Run RNet
        RNetForward(RNetIn,boxes);
        //RNetThreshold
        for (int i=0;i<num;i++)
            if (boxes.get(i).score<RNetThreshold)
                boxes.get(i).deleted=true;
        //Nms
        nms(boxes,0.7f,"Union");
        BoundingBoxReggression(boxes);
        return updateBoxes(boxes);
    }
    /*
     * ONet跑神经网络，将score和bias写入boxes
     */
    private void ONetForward(float[] ONetIn,Vector<Box>boxes){
        int num=ONetIn.length/48/48/3;

        float[] ONetP=new float[num*2]; //prob
        float[] ONetB=new float[num*4]; //bias
        float[] ONetL=new float[num*10]; //landmark

        Object[] outName1=parseOutName(ONetOutName[0]);
        Object[] outName2=parseOutName(ONetOutName[1]);
        Object[] outName3=parseOutName(ONetOutName[2]);

        List<Tensor<?>> outs = session.runner()
                .feed(ONetInName,Tensor.create(new long[]{num,48,48,3}, FloatBuffer.wrap(ONetIn)))
                .fetch((String)outName1[1],(Integer)outName1[0])
                .fetch((String)outName2[1],(Integer)outName2[0])
                .fetch((String)outName3[1],(Integer)outName3[0])
                .run();
        Tensor<?> out=outs.get(0);
        out.writeTo(FloatBuffer.wrap(ONetP));
        out=outs.get(1);
        out.writeTo(FloatBuffer.wrap(ONetB));
        out=outs.get(2);
        out.writeTo(FloatBuffer.wrap(ONetL));

        //转换
        for (int i=0;i<num;i++) {
            //prob
            boxes.get(i).score = ONetP[i * 2 + 1];
            //bias
            for (int j=0;j<4;j++)
                boxes.get(i).bbr[j]=ONetB[i*4+j];

            //landmark
            for (int j=0;j<5;j++) {
                int x=boxes.get(i).left()+(int) (ONetL[i * 10 + j]*boxes.get(i).width());
                int y= boxes.get(i).top()+(int) (ONetL[i * 10 + j +5]*boxes.get(i).height());
                boxes.get(i).landmark[j] = new Point(x,y);
            }
        }
    }
    //ONet
    private Vector<Box> ONet(Mat bitmap,Vector<Box> boxes){
        //ONet Input Init
        int num=boxes.size();
        float[] ONetIn=new float[num*48*48*3];
        float[] curCrop=new float[48*48*3];
        int ONetInIdx=0;
        for (int i=0;i<num;i++){
            crop_and_resize(bitmap,boxes.get(i),48,curCrop);
            flip_diag(curCrop,48,48,3);
            for (int j=0;j<curCrop.length;j++) ONetIn[ONetInIdx++]= curCrop[j];
        }
        //Run ONet
        ONetForward(ONetIn,boxes);
        //ONetThreshold
        for (int i=0;i<num;i++)
            if (boxes.get(i).score<ONetThreshold)
                boxes.get(i).deleted=true;
        BoundingBoxReggression(boxes);
        //Nms
        nms(boxes,0.7f,"Min");
        return updateBoxes(boxes);
    }
    private void square_limit(Vector<Box>boxes,int w,int h){
        //square
        for (int i=0;i<boxes.size();i++) {
            boxes.get(i).toSquareShape();
            boxes.get(i).limit_square(w,h);
        }
    }

    private void flip_diag(float[]data,int h,int w,int stride){
        float[] tmp=new float[w*h*stride];
        for (int i=0;i<w*h*stride;i++) tmp[i]=data[i];
        for (int y=0;y<h;y++)
            for (int x=0;x<w;x++){
                for (int z=0;z<stride;z++)
                    data[(x*h+y)*stride+z]=tmp[(y*w+x)*stride+z];
            }
    }

    //删除做了delete标记的box
    private Vector<Box> updateBoxes(Vector<Box> boxes){
        Vector<Box> b=new Vector<Box>();
        for (int i=0;i<boxes.size();i++)
            if (!boxes.get(i).deleted)
                b.addElement(boxes.get(i));
        return b;
    }

    /*
     * 参数：
     *   bitmap:要处理的图片
     *   minFaceSize:最小的人脸像素值.(此值越大，检测越快)
     * 返回：
     *   人脸框
     */
    private Vector<Box> detectFaces(Mat bitmap, int minFaceSize) {
        long t_start = System.currentTimeMillis();
        //【1】PNet generate candidate boxes
        Vector<Box> boxes=PNet(bitmap,minFaceSize);

        square_limit(boxes,bitmap.cols(),bitmap.rows());System.out.println("PNet:size:  "+boxes.size());
        //【2】RNet
        boxes=RNet(bitmap,boxes);

        square_limit(boxes,bitmap.cols(),bitmap.rows());System.out.println("RNet:size:  "+boxes.size());
        //【3】ONet
        boxes=ONet(bitmap,boxes);
        System.out.println("ONet:size:  "+boxes.size());
        //return
        System.out.println("[*]Mtcnn Detection Time:"+(System.currentTimeMillis()-t_start));
        lastProcessTime=(System.currentTimeMillis()-t_start);
        return boxes;
    }

    /**
     * 人脸检测
     * @param bitmap 要处理的图片
     * @param minFaceSize 最小的人脸像素值.(此值越大，检测越快)
     * @return
     */
    public static Vector<Box> find(Mat bitmap, int minFaceSize){
        return mtcnn.detectFaces(bitmap,minFaceSize);
    }
}
