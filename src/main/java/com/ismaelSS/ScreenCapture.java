package com.ismaelSS;

import com.ismaelSS.layouts.Region;
import javafx.stage.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCapture {

    private static final Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage capture(Region region) {

        double scaleX = Screen.getPrimary().getOutputScaleX();
        double scaleY = Screen.getPrimary().getOutputScaleY();

        int realX = (int) (region.getX() * scaleX);
        int realY = (int) (region.getY() * scaleY) -14;
        int realW = (int) (region.getWidth() * scaleX) +1;
        int realH = (int) (region.getHeight() * scaleY)+1;

        Rectangle area = new Rectangle(realX, realY, realW, realH);

        return robot.createScreenCapture(area);
    }
}