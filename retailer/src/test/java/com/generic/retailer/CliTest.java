package com.generic.retailer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.generic.retailer.domain.Trolley;
import com.generic.retailer.model.Book;
import com.generic.retailer.model.CD;
import com.generic.retailer.model.DVD;
import com.generic.retailer.services.impl.ClientService;
import com.generic.retailer.services.impl.ProductService;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CliTest {

    private ProductService productService;
    private ClientService clientService;
    private Book books = Book.builder().build();
    private CD cds = CD.builder().build();
    private DVD dvds = DVD.builder().build();
    private List<Trolley> trolleyMock;

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
    public void testOnlyReceipt() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        StringWriter writer;

        // Trolley with 3 Products
        trolley3Products();

        Object receipt = MethodUtils.invokeMethod(clientService, true, "buildReceipt", trolleyMock);

        assertTrue(receipt instanceof StringWriter);
        writer = (StringWriter) receipt;
        assertTrue(writer.toString().split(System.lineSeparator()).length > 0);

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
    public void testReceipt()
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        BufferedReader reader = reader("cd", "dvd", "book");
        StringWriter writer = new StringWriter();
        LocalDate notThursday = LocalDate.now();
        if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            notThursday = notThursday.plusDays(1);
        }

        // Prepare Data Tests by Reflection
        FieldUtils.writeField(clientService, "productService", productService, true);
        FieldUtils.writeField(clientService, "date", notThursday, true);
        FieldUtils.writeField(clientService, "reader", reader, true);
        FieldUtils.writeField(clientService, "writer", new BufferedWriter(writer), true);
        FieldUtils.writeField(clientService, "prompt", ">", true);
        MethodUtils.invokeMethod(clientService, true, "initializeProducts");

        // Expects Trolley with 3 Products
        trolley3Products();
        when(productService.findAll()).thenReturn(trolleyMock);

        clientService.run();

        // It builds the receipt for tests
        Object receipt = MethodUtils.invokeMethod(clientService, true, "buildReceipt", trolleyMock);
        assertTrue(receipt instanceof StringWriter);
        writer = (StringWriter) receipt;
        assertTrue(writer.toString().split(System.lineSeparator()).length > 0);

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
    public void testAggregatedReceipt()
            throws IOException, NoSuchFieldException, IllegalAccessException, InvocationTargetException,
                    NoSuchMethodException {
        BufferedReader reader = reader("cd", "cd", "book");

        StringWriter writer = new StringWriter();
        LocalDate notThursday = LocalDate.now();
        if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            notThursday = notThursday.plusDays(1);
        }
        // Prepare Data Tests by Reflection
        FieldUtils.writeField(clientService, "productService", productService, true);
        FieldUtils.writeField(clientService, "date", notThursday, true);
        FieldUtils.writeField(clientService, "reader", reader, true);
        FieldUtils.writeField(clientService, "writer", new BufferedWriter(writer), true);
        FieldUtils.writeField(clientService, "prompt", ">", true);
        MethodUtils.invokeMethod(clientService, true, "initializeProducts");

        // Expects Trolley with Aggregated Products
        trolleyAggregatedProducts();
        when(productService.findAll()).thenReturn(trolleyMock);

        clientService.run();

        // It builds the receipt for tests
        Object receipt = MethodUtils.invokeMethod(clientService, true, "buildReceipt", trolleyMock);
        assertTrue(receipt instanceof StringWriter);
        writer = (StringWriter) receipt;
        assertTrue(writer.toString().split(System.lineSeparator()).length > 0);

        assertReceipt(
                writer,
                "===== RECEIPT ======",
                "CD (x2)       £20.00",
                "BOOK           £5.00",
                "====================",
                "TOTAL         £25.00");
    }

    @Test
    public void testDiscountTwoForOne()
            throws IOException, NoSuchFieldException, IllegalAccessException, InvocationTargetException,
                    NoSuchMethodException {
        BufferedReader reader = reader("dvd", "dvd", "book");

        StringWriter writer = new StringWriter();
        LocalDate notThursday = LocalDate.now();
        if (notThursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            notThursday = notThursday.plusDays(1);
        }
        // Prepare Data Tests by Reflection
        FieldUtils.writeField(clientService, "productService", productService, true);
        FieldUtils.writeField(clientService, "date", notThursday, true);
        FieldUtils.writeField(clientService, "reader", reader, true);
        FieldUtils.writeField(clientService, "writer", new BufferedWriter(writer), true);
        FieldUtils.writeField(clientService, "prompt", ">", true);
        MethodUtils.invokeMethod(clientService, true, "initializeProducts");

        // Expects Trolley with Aggregated Products
        trolleyDiscountTwoForOne();
        when(productService.findAll()).thenReturn(trolleyMock);

        clientService.run();

        // It builds the receipt for tests
        Object receipt = MethodUtils.invokeMethod(clientService, true, "buildReceipt", trolleyMock);
        assertTrue(receipt instanceof StringWriter);
        writer = (StringWriter) receipt;
        assertTrue(writer.toString().split(System.lineSeparator()).length > 0);

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
    public void testDiscountThursdays() throws IOException, NoSuchFieldException, IllegalAccessException {
        BufferedReader reader = reader("dvd", "cd", "book");

        StringWriter writer = new StringWriter();
        LocalDate thursday = LocalDate.now();
        while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            thursday = thursday.plusDays(1);
        }
        ClientService cli = Mockito.mock(ClientService.class);
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
    public void testDiscount2For1OnThursdays() throws IOException, NoSuchFieldException, IllegalAccessException {
        BufferedReader reader = reader("dvd", "dvd", "book");

        StringWriter writer = new StringWriter();
        LocalDate thursday = LocalDate.now();
        while (!thursday.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            thursday = thursday.plusDays(1);
        }
        ClientService cli = Mockito.mock(ClientService.class);
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

    @Before
    public void init() {
        productService = Mockito.mock(ProductService.class);
        books = Book.builder().build();
        cds = CD.builder().build();
        dvds = DVD.builder().build();
        clientService = new ClientService(productService, books, cds, dvds);
    }

    private void trolley3Products() {
        trolleyMock = List.of(new Trolley[] {
            Trolley.builder()
                    .productId(1)
                    .productName("BOOK")
                    .price(5)
                    .quantity(1)
                    .build(),
            Trolley.builder()
                    .productId(1)
                    .productName("DVD")
                    .price(15)
                    .quantity(1)
                    .build(),
            Trolley.builder()
                    .productId(1)
                    .productName("CD")
                    .price(10)
                    .quantity(1)
                    .build()
        });
    }

    private void trolleyAggregatedProducts() {
        trolleyMock = List.of(new Trolley[] {
            Trolley.builder()
                    .productId(1)
                    .productName("BOOK")
                    .price(5)
                    .quantity(1)
                    .build(),
            Trolley.builder()
                    .productId(1)
                    .productName("CD")
                    .price(10)
                    .quantity(1)
                    .build(),
            Trolley.builder()
                    .productId(1)
                    .productName("CD")
                    .price(10)
                    .quantity(1)
                    .build()
        });
    }

    private void trolleyDiscountTwoForOne() {
        trolleyMock = List.of(new Trolley[] {
            Trolley.builder()
                    .productId(1)
                    .productName("BOOK")
                    .price(5)
                    .quantity(1)
                    .build(),
            Trolley.builder()
                    .productId(1)
                    .productName("DVD")
                    .price(15)
                    .quantity(1)
                    .build(),
            Trolley.builder()
                    .productId(1)
                    .productName("DVD")
                    .price(15)
                    .quantity(1)
                    .build()
        });
    }
}
