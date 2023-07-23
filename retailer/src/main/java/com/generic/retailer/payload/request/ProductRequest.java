package com.generic.retailer.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {
    private int productId;
    private String name;
    private double price;
    private int quantity;
}
