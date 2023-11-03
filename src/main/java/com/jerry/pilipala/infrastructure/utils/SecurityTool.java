package com.jerry.pilipala.infrastructure.utils;

import java.security.MessageDigest;
import java.util.Random;

import static com.jerry.pilipala.infrastructure.utils.CaptchaUtil.CharacterConstant.*;


/**
 * @author 22454
 */
public final class SecurityTool {
    /**
     * MD5加密类型
     */
    private static final String MD5_TYPE = "MD5";

    /**
     * 随机生成器
     */
    private static final Random RANDOM_GENERATOR = new Random();

    /**
     * 对文本进行 32 bits MD5 加密
     *
     * @param text 待加密文本
     * @return 加密后文本
     */
    public static String getMd5(String text) throws Exception {
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5_TYPE);
            digest.update(text.getBytes());
            byte[] bytes = digest.digest();
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                int res = b < 0 ? b & 0xff : b;
                if (res < 0x10) {
                    result.append("0");
                }
                result.append(Integer.toHexString(res & 0xff));
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Md5加密失败");
        }

    }

    /**
     * 对文本进行 32 bits 加盐 MD5 加密
     *
     * @param text 待加密文本
     * @param salt 盐
     * @return 加密后文本
     */
    public static String getMd5(String text, String salt) throws Exception {
        StringBuilder textBuilder = new StringBuilder();
        int textIndex = 0;
        int saltIndex = 0;
        while (textIndex != text.length() || saltIndex != salt.length()) {
            if (textIndex < text.length()) {
                textBuilder.append(text.charAt(textIndex++));
            }
            if (saltIndex < salt.length()) {
                textBuilder.append(salt.charAt(saltIndex++));
            }
        }
        return getMd5(textBuilder.toString());
    }

    /**
     * 获取默认 32 bits 盐
     *
     * @return 32 bits salt
     */
    public static String getDefaultLengthSalt() {
        try {
            return getSalt(32);
        } catch (Exception e) {
            //长度大于0永远不可能走这个分支，直接返回就行
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 获取 length 长的盐
     *
     * @param length 长度
     * @return salt
     */
    private static String getSalt(int length) throws Exception {
        if (length <= 0) {
            throw new Exception("Salt生成失败,长度错误：" + length);
        }

        int lowerLength = length > 2 ? RANDOM_GENERATOR.nextInt(length >> 1) + 1 : 1;
        int upperLength = RANDOM_GENERATOR.nextInt((length - lowerLength - 1) > 0 ? (length - lowerLength) >> 1 : 2) + 1;
        int digitLength = RANDOM_GENERATOR.nextInt((length - lowerLength - upperLength - 1) > 0 ? (length - lowerLength - upperLength) >> 1 : 2) + 1;
        int specialCharacterLength = (length - lowerLength - upperLength - digitLength) > 0 ? (length - lowerLength - upperLength - digitLength) : 1;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lowerLength; i++) {
            int nextInt = RANDOM_GENERATOR.nextInt(LOWER_CASE.length);
            int current = i % LOWER_CASE.length;
            swap(LOWER_CASE, current, nextInt);
            result.append(LOWER_CASE[current]);
        }

        for (int i = 0; i < upperLength; i++) {
            int nextInt = RANDOM_GENERATOR.nextInt(UPPER_CASE.length);
            int current = i % UPPER_CASE.length;
            swap(UPPER_CASE, current, nextInt);
            result.append(UPPER_CASE[current]);
        }
        for (int i = 0; i < digitLength; i++) {
            int nextInt = RANDOM_GENERATOR.nextInt(DIGIT.length);
            int current = i % DIGIT.length;
            swap(DIGIT, current, nextInt);
            result.append(DIGIT[current]);
        }
        for (int i = 0; i < specialCharacterLength; i++) {
            int nextInt = RANDOM_GENERATOR.nextInt(SPECIAL_CHARACTER.length);
            int current = i % SPECIAL_CHARACTER.length;
            swap(SPECIAL_CHARACTER, current, nextInt);
            result.append(SPECIAL_CHARACTER[current]);
        }
        if (result.length() > length) {
            result.delete(length, result.length());
        }
        int cycleLength = length > 4 ? RANDOM_GENERATOR.nextInt(length >> 2) : RANDOM_GENERATOR.nextInt(2);
        for (int i = 0; i < cycleLength; i++) {
            int next = RANDOM_GENERATOR.nextInt(result.length());
            char c = result.charAt(i);
            result.setCharAt(i, result.charAt(next));
            result.setCharAt(next, c);
        }
        return result.toString();
    }

    /**
     * 交换 char数组的两个位置的值
     *
     * @param characters 需要修改的数组
     * @param pos1       位置1
     * @param pos2       位置2
     */
    private static void swap(char[] characters, int pos1, int pos2) {
        char c = characters[pos1];
        characters[pos1] = characters[pos2];
        characters[pos2] = c;
    }


}
