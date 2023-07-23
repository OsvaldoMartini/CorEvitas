package com.generic.retailer.services.impl;

import static org.springframework.beans.BeanUtils.copyProperties;

import com.generic.retailer.domain.Database;
import com.generic.retailer.domain.Trolley;
import com.generic.retailer.model.Product;
import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.payload.response.ProductResponse;
import com.generic.retailer.repositories.ProductRepository;
import com.generic.retailer.services.IProductService;
import com.generic.retailer.services.productexception.ProductServiceCustomException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final Database data;

    @Override
    public int addProduct(ProductRequest productRequest) {
        log.info("ProductServiceImpl | addProduct is called");

        Trolley product = Trolley.builder()
                .productId(productRequest.getProductId())
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
    public Optional<ProductResponse> getProductById(int productId) {

        log.info("ProductServiceImpl | getProductById is called");
        log.info("ProductServiceImpl | getProductById | Get the product for productId: {}", productId);

        Optional<Product> prod = data.find(productId);

        Optional<ProductResponse> productResponse = Optional.of(new ProductResponse());
        if (prod.isPresent()) {

            Trolley product = productRepository
                    .findById(productId)
                    .orElse(Trolley.builder()
                            .productId(prod.get().getProductId())
                            .productName(prod.get().getProductName())
                            .price(prod.get().getPrice())
                            .quantity(0)
                            .build());

            copyProperties(product, productResponse.get());

            log.info("ProductServiceImpl | getProductById | productResponse :" + productResponse.toString());

        } else {
            new ProductServiceCustomException("Product with given Id not found", "PRODUCT_NOT_FOUND");
        }
        return productResponse;
    }

    @Override
    public void reduceQuantity(int productId, int quantity) {

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
    public void deleteProductById(int productId) {
        log.info("Product id: {}", productId);

        if (!productRepository.existsById(productId)) {
            log.info("Im in this loop {}", !productRepository.existsById(productId));
            throw new ProductServiceCustomException(
                    "Product with given with Id: " + productId + " not found:", "PRODUCT_NOT_FOUND");
        }
        log.info("Deleting Product with id: {}", productId);
        productRepository.deleteById(productId);
    }

    @Override
    public List<Trolley> findAll() {
        return productRepository.findAll();
    }
}
