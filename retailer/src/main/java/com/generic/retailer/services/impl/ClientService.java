package com.generic.retailer.services.impl;

import com.generic.retailer.domain.Database;
import com.generic.retailer.domain.Trolley;
import com.generic.retailer.model.Product;
import com.generic.retailer.payload.request.ProductRequest;
import com.generic.retailer.payload.response.ProductResponse;
import com.generic.retailer.types.ProductsTypesEnum;
import java.io.*;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    private final Database data;
    private final int maxCol = 20;
    private final NumberFormat ukCurrency = NumberFormat.getCurrencyInstance(Locale.UK);

    @Autowired
    public ClientService(ProductService productService, Database data) {
        this.productService = productService;
        this.data = data;
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

    public void run() throws IOException {
        writeLine("What would you like to buy?");
        prompt();
        Optional<String> line = readLine();
        while (line.isPresent()) {
            if (line.get().equalsIgnoreCase(ProductsTypesEnum.Book.name())) {
                Optional<ProductResponse> resp = productService.getProductById(ProductsTypesEnum.Book.getId());
                if (resp.isPresent()) {
                    int qty = resp.get().getQuantity();
                    productService.addProduct(ProductRequest.builder()
                            .productId(resp.get().getProductId())
                            .name(resp.get().getProductName())
                            .price(resp.get().getPrice())
                            .quantity(++qty)
                            .build());
                }
            } else if (line.get().equalsIgnoreCase(ProductsTypesEnum.CD.name())) {
                Optional<ProductResponse> resp = productService.getProductById(ProductsTypesEnum.CD.getId());
                if (resp.isPresent()) {
                    int qty = resp.get().getQuantity();
                    productService.addProduct(ProductRequest.builder()
                            .productId(resp.get().getProductId())
                            .name(resp.get().getProductName())
                            .price(resp.get().getPrice())
                            .quantity(++qty)
                            .build());
                }
            } else if (line.get().equalsIgnoreCase(ProductsTypesEnum.DVD.name())) {
                Optional<ProductResponse> resp = productService.getProductById(ProductsTypesEnum.DVD.getId());
                if (resp.isPresent()) {
                    int qty = resp.get().getQuantity();
                    productService.addProduct(ProductRequest.builder()
                            .productId(resp.get().getProductId())
                            .name(resp.get().getProductName())
                            .price(resp.get().getPrice())
                            .quantity(++qty)
                            .build());
                }
            }
            writeLine("Would you like anything else?");
            prompt();
            line = readLine();
        }

        buildReceipt(productService.findAll());
    }

    private void printReceipt(StringWriter receipt) {
        String[] obtained = receipt.toString().split(System.lineSeparator());
        Arrays.stream(obtained).forEach(p -> System.out.println(p));
    }

    private StringWriter buildReceipt(List<Trolley> trolley) throws IOException {
        StringWriter receipt = new StringWriter();
        String txtDvdTwoForOne = "";
        double twoFroOneDiscount = 0;
        String txtThursdayDiscount = "";
        double thursdayDiscTotal = 0;
        date = date == null ? LocalDate.now() : date;
        boolean isThursday = date.getDayOfWeek().equals(DayOfWeek.THURSDAY);

        receipt.append(String.format("===== RECEIPT ======%n"));

        Map<String, Long> aggregated = trolley.stream()
                .collect(Collectors.groupingBy(Trolley::getProductName, Collectors.summingLong(Trolley::getQuantity)));
        Iterator<Map.Entry<String, Long>> iterator = aggregated.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String prodName = entry.getKey();
            double prodValue = trolley.stream()
                    .filter(f -> f.getProductName().equalsIgnoreCase(entry.getKey()))
                    .findFirst()
                    .get()
                    .getPrice();
            double itemTotal = prodValue;
            if (entry.getValue() > 1) {
                prodName += " (x" + entry.getValue() + ")";
                itemTotal = prodValue * entry.getValue();
            }

            // appliesTwoForOne for DVDs
            if ((entry.getValue() > 1) && (entry.getKey().equalsIgnoreCase("DVD"))) {
                twoFroOneDiscount = entry.getValue() / 2;
                twoFroOneDiscount = prodValue * twoFroOneDiscount;
                txtDvdTwoForOne = buildText("2 FOR 1", -twoFroOneDiscount);

                long modThursday = entry.getValue() % 2;
                if (modThursday > 0 && isThursday) {
                    thursdayDiscTotal += prodValue * 20 / 100;
                    txtThursdayDiscount = buildText("THURS", -thursdayDiscTotal);
                }
            } else if ((entry.getValue() == 1) && (entry.getKey().equalsIgnoreCase("DVD")) && isThursday) {
                thursdayDiscTotal += (prodValue * entry.getValue()) * 20 / 100;
                txtThursdayDiscount = buildText("THURS", -thursdayDiscTotal);
            }

            if (!entry.getKey().equalsIgnoreCase("DVD") && isThursday) {
                thursdayDiscTotal += (prodValue * entry.getValue()) * 20 / 100;
                txtThursdayDiscount = buildText("THURS", -thursdayDiscTotal);
            }

            receipt.append(buildText(prodName, itemTotal));
        }
        if (!txtDvdTwoForOne.isEmpty()) {
            receipt.append(txtDvdTwoForOne);
        }

        if (!txtThursdayDiscount.isEmpty()) {
            receipt.append(txtThursdayDiscount);
        }

        // Summary
        receipt.append(String.format("====================%n"));
        double totalDouble = summaryReceipt(trolley) - twoFroOneDiscount - thursdayDiscTotal;

        receipt.append(buildText("TOTAL", totalDouble));

        printReceipt(receipt);

        writeLine(String.format(
                "Thank you for visiting Generic Retailer, your total is %s", ukCurrency.format(totalDouble)));

        return receipt;
    }

    private String buildText(String text, double value) {
        String format =
                columnsFormat(maxCol, String.valueOf(ukCurrency.format(value)).length());
        return String.format(format, text, ukCurrency.format(value));
    }

    private double summaryReceipt(List<Trolley> trolley) {
        double sum = trolley.stream()
                .filter(Objects::nonNull)
                .mapToDouble(f -> f.getPrice() * f.getQuantity())
                .sum();
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
        data.add(Product.builder()
                .productId(ProductsTypesEnum.Book.getId())
                .productName("book")
                .price(5)
                .build());
        data.add(Product.builder()
                .productId(ProductsTypesEnum.CD.getId())
                .productName("cd")
                .price(10)
                .build());
        data.add(Product.builder()
                .productId(ProductsTypesEnum.DVD.getId())
                .productName("dvd")
                .price(15)
                .build());
    }
}
// end::code[]
