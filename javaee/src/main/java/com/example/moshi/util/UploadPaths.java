package com.example.moshi.util;

import java.io.File;
import java.io.IOException;

/** 素材文件的落盘目录（放在应用之外，重新部署 WAR 不会丢）。 */
public final class UploadPaths {

    private static final File ROOT = new File(
            System.getProperty("moshi.upload.dir",
                    System.getProperty("user.home") + File.separator + ".moshi" + File.separator + "uploads"));

    private UploadPaths() {
    }

    public static File root() {
        if (!ROOT.exists()) {
            //noinspection ResultOfMethodCallIgnored
            ROOT.mkdirs();
        }
        return ROOT;
    }

    public static File resolve(String storedPath) throws IOException {
        File root = root().getCanonicalFile();
        File file = new File(root, storedPath).getCanonicalFile();
        if (!file.getParentFile().equals(root) || !file.isFile()) {
            throw new IOException("文件不存在");
        }
        return file;
    }
}
