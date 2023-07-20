package com.generic.retailer.services.impl;

import static org.springframework.beans.BeanUtils.copyProperties;

import com.generic.retailer.domain.Trolley;
import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.payload.response.ProductResponse;
import com.generic.retailer.repositories.ProductRepository;
import com.generic.retailer.services.ProductService;
import com.generic.retailer.services.productexception.ProductServiceCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("ProductServiceImpl | addProduct is called");

        Trolley product = Trolley.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();

        product = productRepository.save(product);

        log.info("ProductServiceImpl | addProduct | Product Created");
        log.info("ProductServiceImpl | addProduct | Product Id : " + product.getProductId());
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {

        log.info("ProductServiceImpl | getProductById is called");
        log.info("ProductServiceImpl | getProductById | Get the product for productId: {}", productId);

        Trolley product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new ProductServiceCustomException("Product with given Id not found", "PRODUCT_NOT_FOUND"));

        ProductResponse productResponse = new ProductResponse();

        copyProperties(product, productResponse);

        log.info("ProductServiceImpl | getProductById | productResponse :" + productResponse.toString());

        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {

        log.info("Reduce Quantity {} for Id: {}", quantity, productId);

        Trolley product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new ProductServiceCustomException("Product with given Id not found", "PRODUCT_NOT_FOUND"));

        if (product.getQuantity() < quantity) {
            throw new ProductServiceCustomException(
                    "Product does not have sufficient Quantity", "INSUFFICIENT_QUANTITY");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product Quantity updated Successfully");
    }

    @Override
    public void deleteProductById(long productId) {
        log.info("Product id: {}", productId);

        if (!productRepository.existsById(productId)) {
            log.info("Im in this loop {}", !productRepository.existsById(productId));
            throw new ProductServiceCustomException(
                    "Product with given with Id: " + productId + " not found:", "PRODUCT_NOT_FOUND");
        }
        log.info("Deleting Product with id: {}", productId);
        productRepository.deleteById(productId);
    }
}
