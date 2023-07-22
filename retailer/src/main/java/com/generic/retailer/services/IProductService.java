package com.generic.retailer.services;

import com.generic.retailer.domain.Trolley;
import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.payload.response.ProductResponse;
import java.util.List;

public interface IProductService {

    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);

    public void deleteProductById(long productId);

    List<Trolley> findAll();
}
