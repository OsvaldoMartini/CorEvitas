package com.generic.retailer.services;

import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.payload.response.ProductResponse;

public interface ProductService {

    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);

    public void deleteProductById(long productId);
}
