package com.generic.retailer.domain;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final List<Trolley> data = new ArrayList();

    public void add(Trolley trolley) {
        data.add(trolley);
    }

    public Trolley find(Integer index) {
        return data.get(index);
    }

    public List<Trolley> findAll() {
        return data;
    }
}
