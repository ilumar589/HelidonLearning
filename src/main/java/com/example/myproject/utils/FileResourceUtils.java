package com.example.myproject.utils;

import java.io.InputStream;

public final class FileResourceUtils {

    private FileResourceUtils() {
    }

    public static InputStream getResourceAsStream(final String fileName) {
        // The class loader that loaded the class
        ClassLoader classLoader = FileResourceUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }
}
