package com.ismaelSS.nativewin;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.win32.W32APIOptions;

public class WinOverlayUtil {

    public interface ExtendedUser32 extends User32 {
        ExtendedUser32 INSTANCE = Native.load("user32", ExtendedUser32.class, W32APIOptions.DEFAULT_OPTIONS);
        // Captura o conteúdo da janela para um contexto gráfico (HDC)
        boolean PrintWindow(HWND hWnd, WinDef.HDC hdcBlt, int nFlags);
    }

    public static void makeWindowTransparent(long hwndValue) {
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(hwndValue));

        // Pega o estilo atual da janela
        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);

        // WS_EX_LAYERED: Necessário para janelas com transparência alfa
        exStyle |= WinUser.WS_EX_LAYERED;

        // WS_EX_TRANSPARENT: O segredo! Faz os cliques ignorarem esta janela (Click-through)
        exStyle |= WinUser.WS_EX_TRANSPARENT;

        // WS_EX_NOACTIVATE: Impede que a janela ganhe foco ao ser clicada ou mostrada
        exStyle |= 0x08000000;

        // WS_EX_TOPMOST: Garante que ela fique sempre no topo de todas
        exStyle |= 0x00000008;

        // Aplica o novo estilo
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, exStyle);

        // Define a opacidade (255 = totalmente opaco no conteúdo, mas o clique ainda atravessa)
        User32.INSTANCE.SetLayeredWindowAttributes(hwnd, 0, (byte) 255, WinUser.LWA_ALPHA);
    }
}