package com.generic.retailer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.generic.retailer.services.impl.ProductServiceImpl;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.Test;
import org.mockito.Mockito;

public class CliTest {

    private static BufferedReader reader(String... lines) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(lines).forEach(line -> builder.append(line).append(lineSep()));
        return new BufferedReader(new StringReader(builder.toString()));
    }

    private static String lineSep() {
        return System.lineSeparator();
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
    private static void assertReceipt(StringWriter writer, String... expected) {
        String[] obtained = writer.toString().split(System.lineSeparator());
        int expectedNumItems = expected.length - 3;
        String[] items = new String[expectedNumItems];
        int numItems = 0;
        boolean receiptHeader = false;
        boolean receiptEnd = false;
        String total = "";
        for (int i = 0; i < obtained.length; i++) {
            if (!receiptHeader) {
                if ("===== RECEIPT ======".equals(obtained[i])) {
                    receiptHeader = true;
                }
                // Everything before receipt header is ignored
            } else if (!receiptEnd) {
                if ("====================".equals(obtained[i])) {
                    receiptEnd = true;
                } else {
                    if (numItems == expectedNumItems) {
                        fail("Too many items");
                    }
                    items[numItems] = obtained[i];
                    numItems++;
                }
            } else {
                total = obtained[i];
                break;
            }
        }
        assertThat(receiptHeader).isTrue();
        assertThat(receiptEnd).isTrue();
        assertThat(items).containsExactlyInAnyOrder(Arrays.copyOfRange(expected, 1, expectedNumItems + 1));
        assertThat(total).isEqualTo(expected[expected.length - 1]);
    }

    @Test
    public void testReceipt() throws IOException {

        ProductServiceImpl productServiceImpl = Mockito.mock(ProductServiceImpl.class);

        BufferedReader reader = reader("cd", "dvd", "book");

        StringWriter writer = new StringWriter();
        LocalDate notThursday = LocalDate.now();
        if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            notThursday.plusDays(1);
        }
        Cli cli = Mockito.mock(Cli.class);

        cli.books = Book.builder().build();
        cli.dvds = DVD.builder().build();
        cli.cds = CD.builder().build();
        cli.books.add(Book.builder().name("book").price(5).build());
        cli.cds.add(CD.builder().name("cd").price(10).build());
        cli.dvds.add(DVD.builder().name("dvd").price(15).build());

        cli.create(">", reader, new BufferedWriter(writer), notThursday);
        cli.run();
        assertReceipt(
                writer,
                "===== RECEIPT ======",
                "CD            £10.00",
                "DVD           £15.00",
                "BOOK           £5.00",
                "====================",
                "TOTAL         £30.00");
    }

    @Test
    public void testAggregatedReceipt() throws IOException {
        BufferedReader reader = reader("cd", "cd", "book");

        StringWriter writer = new StringWriter();
        LocalDate notThursday = LocalDate.now();
        if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            notThursday.plusDays(1);
        }
        Cli cli = Mockito.mock(Cli.class);
        cli.create(">", reader, new BufferedWriter(writer), notThursday);
        cli.run();
        System.out.println(writer);
        assertReceipt(
                writer,
                "===== RECEIPT ======",
                "CD (x2)       £20.00",
                "BOOK           £5.00",
                "====================",
                "TOTAL         £25.00");
    }

    @Test
    public void testDiscountTwoForOne() throws IOException {
        BufferedReader reader = reader("dvd", "dvd", "book");

        StringWriter writer = new StringWriter();
        LocalDate notThursday = LocalDate.now();
        if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            notThursday.plusDays(1);
        }
        Cli cli = Mockito.mock(Cli.class);
        cli.create(">", reader, new BufferedWriter(writer), notThursday);
        cli.run();
        assertReceipt(
                writer,
                "===== RECEIPT ======",
                "DVD (x2)      £30.00",
                "BOOK           £5.00",
                "2 FOR 1      -£15.00",
                "====================",
                "TOTAL         £20.00");
    }

    @Test
    public void testDiscountThursdays() throws IOException {
        BufferedReader reader = reader("dvd", "cd", "book");

        StringWriter writer = new StringWriter();
        LocalDate thursday = LocalDate.now();
        while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            thursday.plusDays(1);
        }
        Cli cli = Mockito.mock(Cli.class);
        cli.create(">", reader, new BufferedWriter(writer), thursday);
        cli.run();
        assertReceipt(
                writer,
                "===== RECEIPT ======",
                "DVD           £15.00",
                "CD            £10.00",
                "BOOK           £5.00",
                "THURS         -£6.00",
                "====================",
                "TOTAL         £24.00");
    }

    @Test
    public void testDiscount2For1OnThursdays() throws IOException {
        BufferedReader reader = reader("dvd", "dvd", "book");

        StringWriter writer = new StringWriter();
        LocalDate thursday = LocalDate.now();
        while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            thursday.plusDays(1);
        }
        Cli cli = Mockito.mock(Cli.class);
        cli.create(">", reader, new BufferedWriter(writer), thursday);
        cli.run();
        assertReceipt(
                writer,
                "===== RECEIPT ======",
                "DVD (x2)      £30.00",
                "BOOK           £5.00",
                "2 FOR 1      -£15.00",
                "THURS         -£1.00",
                "====================",
                "TOTAL         £24.00");
    }
}
