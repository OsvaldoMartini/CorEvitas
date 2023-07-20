package com.generic.retailer;

import static java.util.Objects.requireNonNull;

import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.services.impl.ProductServiceImpl;
import com.generic.retailer.types.ProductsTypesEnum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;

public final class Cli implements AutoCloseable {

    @Autowired
    ProductServiceImpl productServiceImpl;

    @Autowired
    Book books;

    @Autowired
    CD cds;

    @Autowired
    DVD dvds;

    public static Cli create(String prompt, BufferedReader reader, BufferedWriter writer, LocalDate date) {
        requireNonNull(prompt);
        requireNonNull(reader);
        requireNonNull(writer);
        return new Cli(prompt, reader, writer, date);
    }

    public static Cli create(BufferedReader reader, BufferedWriter writer) {
        return new Cli(">", reader, writer, LocalDate.now());
    }

    private static final Predicate<String> WHITESPACE =
            Pattern.compile("^\\s{0,}$").asPredicate();

    private final String prompt;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final LocalDate date;

    private Cli(String prompt, BufferedReader reader, BufferedWriter writer, LocalDate date) {
        this.prompt = prompt;
        this.reader = reader;
        this.writer = writer;
        this.date = date;
    }

    private void prompt() throws IOException {
        writeLine(prompt);
    }

    private Optional<String> readLine() throws IOException {
        String line = reader.readLine();
        return line == null || WHITESPACE.test(line) ? Optional.empty() : Optional.of(line);
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
        writer.flush();
    }

    public void run() throws IOException {
        writeLine("What would you like to buy?");
        prompt();
        Optional<String> line = readLine();
        while (line.isPresent()) {

            if (line.get().contains(ProductsTypesEnum.Book.name())) {
                Book book = books.find(1);
                productServiceImpl.addProduct(ProductRequest.builder()
                        .name(book.getName())
                        .price(book.getPrice())
                        .quantity(1)
                        .build());
            } else if (line.get().contains(ProductsTypesEnum.CD.name())) {
                CD cd = cds.find(1);
                productServiceImpl.addProduct(ProductRequest.builder()
                        .name(cd.getName())
                        .price(cd.getPrice())
                        .quantity(1)
                        .build());
            } else if (line.get().contains(ProductsTypesEnum.DVD.name())) {
                CD cd = cds.find(1);
                productServiceImpl.addProduct(ProductRequest.builder()
                        .name(cd.getName())
                        .price(cd.getPrice())
                        .quantity(1)
                        .build());
            }

            writeLine("Would you like anything else?");
            prompt();
            line = readLine();
        }
        writeLine(String.format("Thank you for visiting Generic Retailer, your total is %s", 0));
    }

    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
    }
}
