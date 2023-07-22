package com.generic.retailer.services.impl;

import com.generic.retailer.domain.Trolley;
import com.generic.retailer.model.Book;
import com.generic.retailer.model.CD;
import com.generic.retailer.model.DVD;
import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.types.ProductsTypesEnum;
import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Osvaldo Martini
 */
// tag::code[]
@Component
public class ClientService implements AutoCloseable, CommandLineRunner {

    private String prompt;
    private BufferedReader reader;
    private BufferedWriter writer;
    private LocalDate date;
    private final ProductService productService;
    private final Book books;
    private final CD cds;
    private final DVD dvds;
    private final int maxCol = 20;
    private final NumberFormat ukCurrency = NumberFormat.getCurrencyInstance(Locale.UK);

    @Autowired
    public ClientService(ProductService productService, Book books, CD cds, DVD dvds) {
        this.productService = productService;
        this.books = books;
        this.cds = cds;
        this.dvds = dvds;
    }

    private static final Predicate<String> WHITESPACE =
            Pattern.compile("^\\s{0,}$").asPredicate();

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

    public StringWriter run() throws IOException {
        writeLine("What would you like to buy?");
        prompt();
        Optional<String> line = readLine();
        while (line.isPresent()) {
            if (line.get().equalsIgnoreCase(ProductsTypesEnum.Book.name())) {
                Book book = books.find(0);
                productService.addProduct(ProductRequest.builder()
                        .productId(ProductsTypesEnum.Book.getId())
                        .name(book.getName())
                        .price(book.getPrice())
                        .quantity(1)
                        .build());
            } else if (line.get().equalsIgnoreCase(ProductsTypesEnum.CD.name())) {
                CD cd = cds.find(0);
                productService.addProduct(ProductRequest.builder()
                        .productId(ProductsTypesEnum.CD.getId())
                        .name(cd.getName())
                        .price(cd.getPrice())
                        .quantity(1)
                        .build());
            } else if (line.get().equalsIgnoreCase(ProductsTypesEnum.DVD.name())) {
                DVD dvd = dvds.find(0);
                productService.addProduct(ProductRequest.builder()
                        .productId(ProductsTypesEnum.DVD.getId())
                        .name(dvd.getName())
                        .price(dvd.getPrice())
                        .quantity(1)
                        .build());
            }
            writeLine("Would you like anything else?");
            prompt();
            line = readLine();
        }

        List<Trolley> trolley = productService.findAll();
        StringWriter receipt = buildReceipt(productService.findAll());

        double totalDouble = summaryReceipt(trolley);

        printReceipt(receipt);

        writeLine(String.format(
                "Thank you for visiting Generic Retailer, your total is %s", ukCurrency.format(totalDouble)));

        return receipt;
    }

    private void printReceipt(StringWriter receipt) {
        String[] obtained = receipt.toString().split(System.lineSeparator());
        Arrays.stream(obtained).forEach(p -> System.out.println(p));
    }

    /*
     * The receipt format should be as per below:
     *
     *    "===== RECEIPT ======",
     *    "DVD           £15.00",
     *    "CD            £10.00",
     *    "BOOK           £5.00",
     *    "THURS         -£6.00",
     *    "====================",
     *    "TOTAL         £24.00"
     */
    private StringWriter buildReceipt(List<Trolley> trolley) {
        StringWriter receipt = new StringWriter();
        receipt.append(String.format("===== RECEIPT ======%n"));
        trolley.stream().forEach(p -> {
            String format = columnsFormat(
                    maxCol, String.valueOf(ukCurrency.format(p.getPrice())).length());
            receipt.append(String.format(format, p.getProductName(), ukCurrency.format(p.getPrice())));
        });
        // Summary
        receipt.append(String.format("====================%n"));
        double totalDouble = summaryReceipt(trolley);
        String format = columnsFormat(
                maxCol, String.valueOf(ukCurrency.format(totalDouble)).length());
        receipt.append(String.format(format, "TOTAL", ukCurrency.format(totalDouble)));

        return receipt;
    }

    private double summaryReceipt(List<Trolley> trolley) {
        double sum = trolley.stream().mapToDouble(i -> i.getPrice()).reduce(0, (x, y) -> x + y);
        return sum;
    }

    private String columnsFormat(int maxCol, int rightFactor) {
        int init = maxCol - rightFactor;
        int rightCol = maxCol - init;
        String names = "%1$-" + init + "s";
        String values = "%2$" + rightCol + "s%n";
        String format = names.concat(values);
        return format;
    }

    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
    }

    @Override
    public void run(String... args) throws Exception {
        initializeProducts();
        reader = new BufferedReader(new InputStreamReader(System.in));
        writer = new BufferedWriter(new OutputStreamWriter(System.out));
        prompt = ">";
        run();
    }

    private void initializeProducts() {
        this.books.add(Book.builder().name("book").price(5).build());
        this.cds.add(CD.builder().name("cd").price(10).build());
        this.dvds.add(DVD.builder().name("dvd").price(15).build());
    }
}
// end::code[]