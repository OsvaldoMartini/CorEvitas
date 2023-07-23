package com.generic.retailer.domain;

import com.generic.retailer.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Database {

    private static final List<Product> data = new ArrayList();

    public void add(Product product) {
        data.add(product);
    }

    public Optional<Product> find(Integer index) {
        Optional<Product> product =
                data.stream().filter(d -> d.getProductId().equals(index)).findFirst();
        return product;
    }

    public List<Product> findAll() {
        return data;
    }
}
