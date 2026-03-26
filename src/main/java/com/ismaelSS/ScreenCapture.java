package com.ismaelSS;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCapture {
    public static BufferedImage capture(double x, double y, double w, double h) throws Exception {
        Robot robot = new Robot();

        Rectangle area = new Rectangle(
                (int) x,
                (int) y,
                (int) w,
                (int) h
        );

        return robot.createScreenCapture(area);
    }
}
