package com.ismaelSS;

import net.sourceforge.tess4j.Tesseract;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextExtractor {

    private static final Tesseract tesseract = new Tesseract();

    static {
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");

        tesseract.setTessVariable("tessedit_char_whitelist",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.,!? ");

        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(1);

        tesseract.setTessVariable("preserve_interword_spaces", "1");
        tesseract.setTessVariable("user_defined_dpi", "300");
    }

    public String extract(BufferedImage img) {
        try {
            img = upscale(img);
            img = preprocess(img);

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

        BufferedImage gray = new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics g = gray.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {

                int pixel = gray.getRGB(x, y) & 0xFF;

                if (pixel < 140) {
                    gray.setRGB(x, y, 0x000000);
                } else {
                    gray.setRGB(x, y, 0xFFFFFF);
                }
            }
        }

        return gray;
    }
}