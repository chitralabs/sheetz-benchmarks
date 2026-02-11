package io.github.chitralabs.sheetz.benchmarks.comparison;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import io.github.chitralabs.sheetz.benchmarks.model.ProductEasyExcel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyExcel: Write + Read in ~30 lines of actual code.
 * Requires a separate annotated model and a listener pattern for reading.
 */
public class EasyExcelComparison {

    public static void main(String[] args) throws Exception {
        new File("output").mkdirs();
        String filePath = "output/easyexcel_products.xlsx";

        List<ProductEasyExcel> products = Arrays.asList(
                new ProductEasyExcel("Laptop Pro 15", 1299.99, true, "Electronics", 45),
                new ProductEasyExcel("Wireless Mouse", 29.99, true, "Accessories", 230),
                new ProductEasyExcel("USB-C Hub", 49.99, false, "Accessories", 0),
                new ProductEasyExcel("Mechanical Keyboard", 89.99, true, "Accessories", 120),
                new ProductEasyExcel("4K Monitor", 599.99, true, "Electronics", 35),
                new ProductEasyExcel("Webcam HD", 79.99, false, "Electronics", 0),
                new ProductEasyExcel("Laptop Stand", 39.99, true, "Furniture", 85),
                new ProductEasyExcel("Desk Lamp", 24.99, true, "Furniture", 150),
                new ProductEasyExcel("Noise Cancelling Headphones", 249.99, true, "Audio", 60),
                new ProductEasyExcel("Portable Speaker", 59.99, false, "Audio", 0)
        );

        // ── Write ──────────────────────────────────────────
        EasyExcel.write(filePath, ProductEasyExcel.class)
                .sheet("Products")
                .doWrite(products);

        // ── Read (requires listener pattern) ───────────────
        List<ProductEasyExcel> result = new ArrayList<>();
        EasyExcel.read(filePath, ProductEasyExcel.class, new ReadListener<ProductEasyExcel>() {
            @Override
            public void invoke(ProductEasyExcel data, AnalysisContext context) {
                result.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // no-op
            }
        }).sheet().doRead();

        // ── Print ──────────────────────────────────────────
        System.out.println("=== EasyExcel ===");
        System.out.println("Wrote " + products.size() + " products");
        System.out.println("Read back " + result.size() + " products:");
        result.forEach(p -> System.out.printf("  %-30s $%8.2f  inStock=%-5s  qty=%d%n",
                p.getName(), p.getPrice(), p.getInStock(), p.getQuantity()));
    }
}
