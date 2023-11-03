package com.jerry.pilipala.infrastructure.utils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import static com.jerry.pilipala.infrastructure.utils.CaptchaUtil.CharacterConstant.*;


public class CaptchaUtil {
    public static class CharacterConstant {
        /**
         * 小写字符数组
         */
        public static final char[] LOWER_CASE = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        /**
         * 大写字母数组
         */
        public static final char[] UPPER_CASE = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        /**
         * 数字数组
         */
        public static final char[] DIGIT = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        /**
         * 特殊符号数组
         */
        public static final char[] SPECIAL_CHARACTER = {'~', ';', '<', '@', '#', ':', '>', '%', '^'};
    }
    
    //图片宽高
    private static final int WIDTH = 200;
    private static final int HEIGHT = 50;
    //噪点
    private static final float YAWN_RATE = 0.05f;

    public static String generatorCaptchaWithCharAndNumberByLength(int length) {
        int lowerLength = ThreadLocalRandom.current().nextInt(length);
        int upperLength = ThreadLocalRandom.current().nextInt(length - lowerLength);
        int numberLength = length - lowerLength - upperLength;
        StringBuilder builder = new StringBuilder(length);
        int randomIndex;
        for (int i = 0; i < lowerLength; i++) {
            randomIndex = ThreadLocalRandom.current().nextInt(LOWER_CASE.length);
            builder.append(LOWER_CASE[randomIndex]);
        }

        for (int i = 0; i < upperLength; i++) {
            randomIndex = ThreadLocalRandom.current().nextInt(UPPER_CASE.length);
            builder.append(UPPER_CASE[randomIndex]);
        }
        for (int i = 0; i < numberLength; i++) {
            randomIndex = ThreadLocalRandom.current().nextInt(DIGIT.length);
            builder.append(DIGIT[randomIndex]);
        }
        return builder.toString();
    }

    public static String generatorCaptchaNumberByLength(int length) {
        StringBuilder result = new StringBuilder();
        int randomIndex;
        for (int i = 0; i < length; i++) {
            randomIndex = ThreadLocalRandom.current().nextInt(DIGIT.length);
            result.append(DIGIT[randomIndex]);
        }
        return result.toString();
    }

    public static Pair<String, String> generatorBase64CaptchaStringByLength(int length) throws Exception {
        String captchaWithCharAndNumberByLength = generatorCaptchaWithCharAndNumberByLength(length);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //绘制边框
        graphics.setColor(Color.GRAY);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        //设置背景色 bc > fc
        int fc = ThreadLocalRandom.current().nextInt(185) + 50;
        int bc = ThreadLocalRandom.current().nextInt(20) + 205;
        if (fc >= bc) {
            bc += fc - bc + 5;
        }
        Color c = getRandomColor(fc, bc);
        graphics.setColor(c);
        graphics.fillRect(0, 2, WIDTH, HEIGHT - 4);

        //绘制干扰线
        graphics.setColor(getRandomColor(160, 200));
        int lineCount = ThreadLocalRandom.current().nextInt(20);
        for (int i = 0; i < lineCount; i++) {
            int x = ThreadLocalRandom.current().nextInt(WIDTH - 1);
            int y = ThreadLocalRandom.current().nextInt(HEIGHT - 1);
            int x1 = ThreadLocalRandom.current().nextInt(6) + 1;
            int y1 = ThreadLocalRandom.current().nextInt(12) + 1;
            graphics.drawLine(x, y, x + x1 + 40, y + y1 + 20);
        }

        //添加噪点
        int area = (int) (YAWN_RATE * WIDTH * HEIGHT);
        for (int i = 0; i < area; i++) {
            int x = ThreadLocalRandom.current().nextInt(WIDTH);
            int y = ThreadLocalRandom.current().nextInt(HEIGHT);
            int rgb = getRandomIntColor();
            image.setRGB(x, y, rgb);
        }

        //扭曲图片
        shear(graphics, c);

        graphics.setColor(getRandomColor(100, 160));
        int fontSize = HEIGHT - 4;
//        Font font = new Font("Algerian", Font.PLAIN, fontSize);
        Font font = new Font("宋体", Font.PLAIN, fontSize);
        graphics.setFont(font);
        char[] chars = captchaWithCharAndNumberByLength.toCharArray();
        for (int i = 0; i < length; i++) {
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.setToRotation(Math.PI / 4 * ThreadLocalRandom.current().nextDouble() * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1),
                    (WIDTH / (length * 1.0)) * i + fontSize / 2.0, HEIGHT / 2.0);
            graphics.setTransform(affineTransform);
            graphics.drawChars(chars, i, 1, ((WIDTH - 10) / length) * i + 5, HEIGHT / 2 + fontSize / 2 - 10);
        }
        graphics.dispose();

        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = null;

        StringBuilder result = new StringBuilder();
        try {
            imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
            ImageIO.write(image, "png", imageOutputStream);
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            String base64 = Base64Util.generateBase64Jpeg(inputStream);
            result.append(base64);
            return new Pair<>(captchaWithCharAndNumberByLength, result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                byteArrayOutputStream.close();
                if (imageOutputStream != null) {
                    imageOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new Exception("生成验证码失败");

    }

    private static Color getRandomColor(int fc, int bc) {
        fc &= 255;
        bc &= 255;
//        System.out.printf("fc: %s\tbc: %s\t bc>fc:%s  \n", fc, bc, bc > fc);
        int r = fc + ThreadLocalRandom.current().nextInt(bc - fc);
        int g = fc + ThreadLocalRandom.current().nextInt(bc - fc);
        int b = fc + ThreadLocalRandom.current().nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static int getRandomIntColor() {
        int[] rgb = getRandomRgb();
        int color = 0;
        for (int c : rgb) {
            color = color << 8;
            color |= c;
        }
        return color;
    }

    private static int[] getRandomRgb() {
        int[] rgb = new int[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = ThreadLocalRandom.current().nextInt(255);
        }
        return rgb;
    }

    private static void shear(Graphics g, Color color) {
        shearX(g, color);
        shearY(g, color);
    }

    private static void shearX(Graphics g, Color color) {

        int period = ThreadLocalRandom.current().nextInt(4);

        int frames = 1;
        int phase = ThreadLocalRandom.current().nextInt(2);

        for (int i = 0; i < CaptchaUtil.HEIGHT; i++) {
            double d = (double) (period / 2)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(0, i, CaptchaUtil.WIDTH, 1, (int) d, 0);
            g.setColor(color);
            g.drawLine((int) d, i, 0, i);
            g.drawLine((int) d + CaptchaUtil.WIDTH, i, CaptchaUtil.WIDTH, i);
        }

    }

    private static void shearY(Graphics g, Color color) {
        // 50;
        int period = ThreadLocalRandom.current().nextInt(40) + 10;

        boolean borderGap = true;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < CaptchaUtil.WIDTH; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, CaptchaUtil.HEIGHT, 0, (int) d);
            g.setColor(color);
            g.drawLine(i, (int) d, i, 0);
            g.drawLine(i, (int) d + CaptchaUtil.HEIGHT, i, CaptchaUtil.HEIGHT);
        }
    }

}
