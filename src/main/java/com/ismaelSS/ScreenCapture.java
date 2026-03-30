package com.ismaelSS;

import com.ismaelSS.layouts.Region;
import com.ismaelSS.nativewin.WinOverlayUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ScreenCapture {

    public static BufferedImage captureWindowRegion(long hwndValue, Region region) {
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(hwndValue));

        // 1. Pegar dimensões reais da janela
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        int winWidth = rect.right - rect.left;
        int winHeight = rect.bottom - rect.top;

        // 2. Preparar contexto de memória
        WinDef.HDC hdcWindow = User32.INSTANCE.GetDC(hwnd);
        WinDef.HDC hdcMem = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
        WinDef.HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, winWidth, winHeight);
        com.sun.jna.platform.win32.WinNT.HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMem, hBitmap);

        // 3. Capturar conteúdo da janela
        WinOverlayUtil.ExtendedUser32.INSTANCE.PrintWindow(hwnd, hdcMem, 0x2);

        // 4. Criar BufferedImage
        BufferedImage fullWindow = new BufferedImage(winWidth, winHeight, BufferedImage.TYPE_INT_RGB);

        WinGDI.BITMAPINFO bi = new WinGDI.BITMAPINFO();
        bi.bmiHeader.biSize = bi.bmiHeader.size();
        bi.bmiHeader.biWidth = winWidth;
        bi.bmiHeader.biHeight = -winHeight; // imagem top-down
        bi.bmiHeader.biPlanes = 1;
        bi.bmiHeader.biBitCount = 32;
        bi.bmiHeader.biCompression = WinGDI.BI_RGB;

        int[] pixels = ((DataBufferInt) fullWindow.getRaster().getDataBuffer()).getData();

        // Alocar memória nativa
        Memory buffer = new Memory(pixels.length * 4);

        // Copiar bitmap para memória nativa
        GDI32.INSTANCE.GetDIBits(
                hdcMem, // ⚠️ corrigido: usar hdcMem
                hBitmap,
                0,
                winHeight,
                buffer,
                bi,
                WinGDI.DIB_RGB_COLORS
        );

        // Copiar da memória nativa para o array Java
        buffer.read(0, pixels, 0, pixels.length);

        // Limpeza
        GDI32.INSTANCE.SelectObject(hdcMem, hOld);
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMem);
        User32.INSTANCE.ReleaseDC(hwnd, hdcWindow);

        // 5. Recortar região
        int regX = (int) region.getX() +10;
        int regY = (int) region.getY() -3;
        int regW = (int) region.getWidth() -2;
        int regH = (int) region.getHeight();

        try {
            return fullWindow.getSubimage(regX, regY, regW, regH);
        } catch (Exception e) {
            return fullWindow;
        }
    }
}