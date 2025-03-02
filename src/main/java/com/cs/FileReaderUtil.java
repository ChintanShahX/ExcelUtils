package com.cs;

import java.io.File;
import java.net.URL;

public class FileReaderUtil {

    public static File getFileFromResources(String fileName) {
        ClassLoader classLoader = FileReaderUtil.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return new File(resource.getFile());
    }

    public static File getFileFromLocation(String filePath) {
        return new File(filePath);
    }
}
