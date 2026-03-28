package com.ismaelSS;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TextExtractor {

    private static final Tesseract tesseract = new Tesseract();

    static {
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");

        tesseract.setVariable("tessedit_char_whitelist",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.,!? ");

        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(1);

        tesseract.setVariable("preserve_interword_spaces", "1");
        tesseract.setVariable("user_defined_dpi", "300");
    }

    public String extract(BufferedImage img) {
        try {

            ImageIO.write(img, "png", new File("original.png"));
            img = adjustContrast(img,5f);
            ImageIO.write(img, "png", new File("contrat5.png"));

            return tesseract.doOCR(img);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private BufferedImage upscale(BufferedImage img) {

        int scale = 2;

        BufferedImage scaled = new BufferedImage(
                img.getWidth() * scale,
                img.getHeight() * scale,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = scaled.createGraphics();

        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        g2d.drawImage(img, 0, 0,
                scaled.getWidth(),
                scaled.getHeight(),
                null
        );

        g2d.dispose();

        return scaled;
    }

    private BufferedImage preprocess(BufferedImage img) {
        BufferedImage result = new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {

                int rgb = img.getRGB(x, y);

                // Extract RGB components
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convert RGB to HSL
                float[] hsl = rgbToHsl(r, g, b);
                float lightness = hsl[2]; // Lightness component (0-1)

                // Apply threshold based on lightness
                if (lightness < 0.55f) { // 0.55 corresponds approximately to 140 in grayscale
                    result.setRGB(x, y, 0x000000);
                } else {
                    result.setRGB(x, y, 0xFFFFFF);
                }
            }
        }

        return result;
    }

    private float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        // Calculate Hue
        float hue = 0;
        if (delta != 0) {
            if (max == rf) {
                hue = (gf - bf) / delta;
            } else if (max == gf) {
                hue = 2 + (bf - rf) / delta;
            } else {
                hue = 4 + (rf - gf) / delta;
            }
            hue *= 60;
            if (hue < 0) hue += 360;
        }

        // Calculate Lightness
        float lightness = (max + min) / 2;

        // Calculate Saturation
        float saturation = 0;
        if (delta != 0) {
            saturation = delta / (1 - Math.abs(2 * lightness - 1));
        }

        return new float[] {hue, saturation, lightness};
    }

    public static BufferedImage adjustContrast(BufferedImage image, float contrast) {
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));

                int r = adjustChannel(color.getRed(), contrast);
                int g = adjustChannel(color.getGreen(), contrast);
                int b = adjustChannel(color.getBlue(), contrast);

                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

    private static int adjustChannel(int value, float contrast) {
        int adjusted = (int) ((value - 128) * contrast + 128);
        return Math.max(0, Math.min(255, adjusted));
    }

}