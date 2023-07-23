package com.generic.retailer.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public final class CD {
    private int id;
    private String name;
    private double price;

    private static final List<CD> cds = new ArrayList();

    public void add(CD CD) {
        cds.add(CD);
    }

    public CD find(int index) {
        return cds.get(index);
    }

    public List<CD> findAll() {
        return cds;
    }
}
