package com.facenet.utils;

/**
 * Created by sh
 * 2020/3/4.
 */
public class FilePath {
    //TODO 将path保存在yml中，便于更换
    private static final String PATH = "C:/facenet/";
    public static String getResource(String name){
//        String path = null;
//        try {
//            path = new ClassPathResource(name).getFile().getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        String path = Thread.currentThread().getContextClassLoader().getResource(name).getPath();
//        System.out.println(path);

        return PATH+name;
//        return FilePath.class.getClassLoader().getResource(name).getPath();
    }
}
