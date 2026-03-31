package com.ismaelSS;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HotkeyManager implements NativeKeyListener {

    private final HotkeyCallbacks callbacks;

    public interface HotkeyCallbacks {
        void onQuickCaptureRequested();
        void onClearOverlaysRequested();
    }

    public HotkeyManager(HotkeyCallbacks callbacks) {
        this.callbacks = callbacks;
        setup();
    }

    private void setup() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        boolean ctrl = (e.getModifiers() & NativeKeyEvent.CTRL_L_MASK) != 0;
        boolean shift = (e.getModifiers() & NativeKeyEvent.SHIFT_L_MASK) != 0;

        //SHIFT + 1 -> Captura Rápida
        if (shift && e.getKeyCode() == NativeKeyEvent.VC_1) {
            callbacks.onQuickCaptureRequested();
        }

        // SHIFT + 2 -> Limpar
        if (shift && e.getKeyCode() == NativeKeyEvent.VC_2) {
            callbacks.onClearOverlaysRequested();
        }
    }
}