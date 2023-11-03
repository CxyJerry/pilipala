package com.jerry.pilipala.infrastructure.utils;



import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * @author 22454
 */
public final class Base64Util {
    private static final String JPEG_PREFIX = "data:img/jpeg;base64,";

    public static String generateBase64(ByteArrayOutputStream byteArrayOutputStream) {
        ImageOutputStream imageOutputStream = null;
        InputStream inputStream = null;
        try {
            imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return generateBase64(inputStream);
        } catch (Exception e) {
            throw new BusinessException("base64生成失败", StandardResponse.ERROR);
        } finally {
            try {
                if (imageOutputStream != null) {
                    imageOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateBase64Jpeg(ByteArrayOutputStream byteArrayOutputStream) {
        return JPEG_PREFIX + generateBase64(byteArrayOutputStream);
    }

    public static String generateBase64Jpeg(InputStream inputStream) {
        return JPEG_PREFIX + generateBase64(inputStream);
    }

    public static String generateBase64(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            int read = inputStream.read(bytes);
            inputStream.close();
            return new String(Base64.getEncoder().encode(bytes));
        } catch (Exception e) {
            throw new BusinessException("base64生成失败", StandardResponse.ERROR);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static byte[] base64ToImageByteArray(String base64) {
        try {
            base64 = base64.replaceFirst("^data:image/\\w+;base64,", "");
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            for (int i = 0; i < imageBytes.length; i++) {
                if (imageBytes[i] < 0) {
                    imageBytes[i] += 256;
                }
            }
            return imageBytes;
        } catch (Exception e) {
            throw new BusinessException("base64转图片失败", StandardResponse.ERROR);
        }
    }

}
