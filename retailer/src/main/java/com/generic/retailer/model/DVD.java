package com.generic.retailer.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DVD {
    private int id;
    private String name;
    private double price;
    private static final List<DVD> dvds = new ArrayList();

    public void add(DVD DVD) {
        dvds.add(DVD);
    }

    public DVD find(int index) {
        return dvds.get(index);
    }

    public List<DVD> findAll() {
        return dvds;
    }
}
