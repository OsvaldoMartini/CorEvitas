package com.generic.retailer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {
    private Integer productId;
    private String productName;
    private double price;
}
