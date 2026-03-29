package com.ismaelSS.layouts;

import java.util.ArrayList;
import java.util.List;


import java.util.ArrayList;
import java.util.List;

public class Layout {

    private String name;
    private List<Region> regions = new ArrayList<>();

    public Layout() {} // 🔥 obrigatório

    public Layout(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Region> getRegions() { return regions; }

    public void setName(String name) { this.name = name; }
    public void setRegions(List<Region> regions) { this.regions = regions; }

    @Override
    public String toString() {
        return name;
    }
}