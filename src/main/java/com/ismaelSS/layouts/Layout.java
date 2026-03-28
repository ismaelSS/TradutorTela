package com.ismaelSS.layouts;

import java.util.ArrayList;
import java.util.List;

public class Layout {

    private String name;
    private List<Region> regions = new ArrayList<>();

    public Layout(String name) {
        this.name = name;
    }

    public void addRegion(Region region) {
        regions.add(region);
    }
}