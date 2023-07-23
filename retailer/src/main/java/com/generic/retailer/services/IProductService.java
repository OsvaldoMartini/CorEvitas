package com.generic.retailer.services;

import com.generic.retailer.domain.Trolley;
import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.payload.response.ProductResponse;
import java.util.List;
import java.util.Optional;

public interface IProductService {

    int addProduct(ProductRequest productRequest);

    Optional<ProductResponse> getProductById(int productId);

    void reduceQuantity(int productId, int quantity);

    public void deleteProductById(int productId);

    List<Trolley> findAll();
}
