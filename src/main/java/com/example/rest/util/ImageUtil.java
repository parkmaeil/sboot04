package com.example.rest.util;

import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageUtil {

    public static String makePath(String uploadPath, String fileName,  Long book_id) throws IOException {
        String path=uploadPath+book_id;
        Files.createDirectories(Paths.get(path));
        return new File(path).getAbsolutePath()+"\\"+fileName; // 업로드할 파일의 절대 경로를 만들기
    }
    public static BufferedImage getThumbnail(MultipartFile originFile, Integer width) throws IOException{
        BufferedImage thumbImg=null;
        BufferedImage img= ImageIO.read(originFile.getInputStream());
        thumbImg= Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
        return thumbImg; // 썸네일 이미지
    }

    public static Path getFileAsResource(String uploadPath, Long book_id, String file_name) throws IOException {
        String location = uploadPath + book_id + "\\" + file_name;
        File file = new File(location);
        if (file.exists()) {
            Path path = Paths.get(file.getAbsolutePath());
            return path;
        } else {
            return null;
        }
    }

    public static boolean deleteImage(String uploadPath, Long book_id, String fileName) {
        try {
            File f = new File(uploadPath + book_id + "\\" +fileName); // file to be delete
            if (f.delete())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFolder(String uploadPath, Long book_id) {
        File directory = new File(uploadPath + book_id);
        if (!directory.exists()) {
            return false; // The directory doesn't exist
        }
        try {
            // List all files in the directory
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        // If deletion of any file fails, return false
                        return false;
                    }
                }
            }
            // After all files are deleted, delete the directory itself
            return directory.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
