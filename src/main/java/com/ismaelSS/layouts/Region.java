package com.ismaelSS.layouts;

import java.awt.*;

public class Region {
    private double x, y, width, height;

    public Region() {} // 🔥 obrigatório pro Jackson

    public Region(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }

    @Override
    public String toString() {
//        return String.format("Região (%.0f, %.0f)", x, y);
        return String.format("(%.0f, %.0f)", x, y);
    }
}