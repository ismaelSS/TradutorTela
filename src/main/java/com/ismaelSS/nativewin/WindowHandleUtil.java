package com.ismaelSS.nativewin;


import javafx.stage.Stage;

import java.lang.reflect.Method;

public class WindowHandleUtil {

    public static long getHWND(Stage stage) {
        try {
            Method getPeer = javafx.stage.Window.class.getDeclaredMethod("getPeer");
            getPeer.setAccessible(true);
            Object tkStage = getPeer.invoke(stage);

            Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow");
            getPlatformWindow.setAccessible(true);
            Object platformWindow = getPlatformWindow.invoke(tkStage);

            Method getNativeHandle = platformWindow.getClass().getMethod("getNativeHandle");
            return (long) getNativeHandle.invoke(platformWindow);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}