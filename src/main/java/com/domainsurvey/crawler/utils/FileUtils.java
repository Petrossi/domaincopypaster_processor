package com.domainsurvey.crawler.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.springframework.util.FileSystemUtils;
import com.google.common.io.Files;

public class FileUtils {

    public static File createFile(String filePath) throws IOException {
        File newFile = new File(filePath);

        Files.createParentDirs(newFile);
        Files.touch(newFile);

        return newFile;
    }

    public static File createNewCleanFile(String filePath) throws IOException {
        deleteFile(filePath);

        return createFile(filePath);
    }

    public static void deleteDirectory(String filePath){
        FileSystemUtils.deleteRecursively(new File(filePath));
    }

    public static void createDirectory(String filePath){
        new File(filePath).mkdirs();
    }

    public static void deleteFile(String filePath){
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
    }

    public static boolean isDirectoryExists(String directoryPath) {
        return Paths.get(directoryPath).toFile().isDirectory();
    }

    public static void writeToFile(String path, String content){
        try {
            FileUtils.createFile(path);

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
                writer.write(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFileAsString(String fileName) throws Exception {
        return readFileAsString(Paths.get(fileName).toFile());
    }

    public static String readFileAsString(File file) throws Exception {
        String data;
        try {
            data = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } catch (java.nio.file.NoSuchFileException e) {
            data = "";
        }

        return data;
    }

    public static void renameDirectory(String from, String to){
        deleteDirectory(to);

        if(isDirectoryExists(from)){
            new File(from).renameTo(new File(to));
        }
    }
}