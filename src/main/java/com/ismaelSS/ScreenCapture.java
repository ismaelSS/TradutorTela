package com.ismaelSS;
import javafx.stage.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCapture {
    public static BufferedImage capture(double x, double y, double w, double h) throws Exception {
        Robot robot = new Robot();
        double scaleX = Screen.getPrimary().getOutputScaleX();
        double scaleY = Screen.getPrimary().getOutputScaleY();
        Rectangle area = new Rectangle(
                (int) (x * scaleX),
                (int) ((y-13) * scaleY),
                (int) ((w + 1)* scaleX),
                (int) ((h + 1) * scaleY)
        );

        return robot.createScreenCapture(area);
    }
}
