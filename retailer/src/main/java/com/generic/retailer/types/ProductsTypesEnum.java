package com.generic.retailer.types;

import java.util.stream.Stream;

public enum ProductsTypesEnum {
    Book("Book"),
    CD("Cd"),
    DVD("DVD");

    private String typeOfProduct;

    ProductsTypesEnum(String typeOfProduct) {
        this.typeOfProduct = typeOfProduct;
    }

    public String getTypeOfProduct() {
        return typeOfProduct;
    }

    public void setTypeOfProduct(String typeOfProduct) {
        this.typeOfProduct = typeOfProduct;
    }

    public static Stream<ProductsTypesEnum> stream() {
        return Stream.of(ProductsTypesEnum.values());
    }
}
