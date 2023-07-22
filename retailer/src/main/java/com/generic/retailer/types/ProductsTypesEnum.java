package com.generic.retailer.types;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum ProductsTypesEnum {
    Book("BOOK", 1),
    CD("CD", 2),
    DVD("DVD", 3);

    private final String typeOfProduct;
    private final long id;

    ProductsTypesEnum(String typeOfProduct, long id) {
        this.typeOfProduct = typeOfProduct;
        this.id = id;
    }

    public String getType() {
        return typeOfProduct;
    }

    public static Stream<ProductsTypesEnum> stream() {
        return Stream.of(ProductsTypesEnum.values());
    }

    public long getId() {
        return id;
    }

    // Reverse lookup methods
    public static Optional<ProductsTypesEnum> getProductsTypesEnumByValue(String value) {
        return Arrays.stream(ProductsTypesEnum.values())
                .filter(accStatus -> accStatus.typeOfProduct.equals(value))
                .findFirst();
    }
}
