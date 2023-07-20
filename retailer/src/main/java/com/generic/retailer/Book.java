package com.generic.retailer;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Value
@Builder(toBuilder = true)
public class Book {
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
