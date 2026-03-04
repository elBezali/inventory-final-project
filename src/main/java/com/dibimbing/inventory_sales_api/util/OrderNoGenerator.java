package com.dibimbing.inventory_sales_api.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class OrderNoGenerator {
    private static final AtomicLong SEQ = new AtomicLong(0);

    public static String next() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long n = SEQ.incrementAndGet();
        return "SO-" + date + "-" + String.format("%06d", n);
    }
}