package com.facenet.utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by yl
 * 2020/3/4.
 */
public class ImgUtil {
    private static BASE64Decoder decoder = new BASE64Decoder();
    /**
     * 图片字符串进行Base64解码并生成图片
     * @param data 字符串
     * @param path 保存地址
     * @return
     */
    public static boolean saveImage(String data, String path) {
        if (data == null) // 图像数据为空
            return false;

        try {
            // Base64解码
            byte[] bytes = decoder.decodeBuffer(data);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(path);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Mat base2Mat(String data) {
        if (data == null || "".equals(data))
            return null;
        if(data.indexOf("data:image/")==0 && data.contains("base64,")){//头部有js生成标签
            data=data.substring(data.indexOf("base64,")+7);//去掉头部标签
        }

        try {
            // Base64解码
            byte[] bytes = decoder.decodeBuffer(data);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            return img2Mat(bufferedImage);
        } catch (Exception e) {
            System.out.println("base2Mat error");
            return null;
        }
    }

    public static Mat img2Mat (BufferedImage original) {
        return img2Mat(original,original.getType(), CvType.CV_8UC3);
    }
    public static Mat img2Mat (BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() != imgType) {

            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);

            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
        mat.put(0, 0, pixels);
        return mat;
    }
}
