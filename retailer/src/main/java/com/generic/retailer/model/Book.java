package com.generic.retailer.model;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Value
@Builder
public class Book {
    private long id;
    private String name;
    private double price;

    private static final List<Book> books = new ArrayList();

    public void add(Book book) {
        books.add(book);
    }

    public Book find(Integer index) {
        return books.get(index);
    }

    public List<Book> findAll() {
        return books;
    }
}
