package org._ubb.utils;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtils {

    public static String generateNodeIdentifier(String host, int port) {
        return DigestUtils.sha256Hex(host + ":" + port);
    }

    public static String generateFileId(String filepath) throws IOException, NoSuchAlgorithmException {
        File file = new File(filepath);

        if (!file.exists()) {
            throw new IOException("File does not exist: " + filepath);
        }

        BasicFileAttributes attrs = Files.readAttributes(Paths.get(filepath), BasicFileAttributes.class);
        String metadata = file.getName() + "_" + attrs.size() + "_" + attrs.lastModifiedTime();

        // Generate file identifier using SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(metadata.getBytes());

        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
