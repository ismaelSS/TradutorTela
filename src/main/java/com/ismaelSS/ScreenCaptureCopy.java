package com.ismaelSS;

import com.ismaelSS.layouts.Region;
import com.sun.jna.platform.win32.User32;
import javafx.stage.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCaptureCopy {

    private static final Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage capture(Region region) {
        //TODOScreenCapture
        list();

        double scaleX = Screen.getPrimary().getOutputScaleX();
        double scaleY = Screen.getPrimary().getOutputScaleY();

        int realX = (int) (region.getX() * scaleX);
        int realY = (int) (region.getY() * scaleY) -14;
        int realW = (int) (region.getWidth() * scaleX) +1;
        int realH = (int) (region.getHeight() * scaleY)+1;

        Rectangle area = new Rectangle(realX, realY, realW, realH);

        return robot.createScreenCapture(area);
    }

    //TODO: retirar essa funcao
    public static void list() {
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            // Filtra apenas janelas visíveis para não poluir o console
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                String title = new String(windowText).trim();

                if (!title.isEmpty()) {
                    System.out.println("HWND: " + hwnd + " - Título: " + title);
                }
            }
            return true; // Continua a enumeração
        }, null);
    }


}