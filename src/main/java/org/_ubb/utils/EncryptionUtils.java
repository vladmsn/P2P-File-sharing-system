package org._ubb.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class EncryptionUtils {

    public static String generateNodeIdentifier(String host, int port) {
        return DigestUtils.sha256Hex(host + ":" + port);
    }

}
