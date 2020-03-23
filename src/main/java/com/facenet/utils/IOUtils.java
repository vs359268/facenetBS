package com.facenet.utils;

import java.io.*;

/**
 * Created by sh
 * 2020/3/5.
 */
public class IOUtils {
    public static byte[] readFileByBytes(String fileName) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = new FileInputStream(fileName);
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        return out.toByteArray();
    }
}
