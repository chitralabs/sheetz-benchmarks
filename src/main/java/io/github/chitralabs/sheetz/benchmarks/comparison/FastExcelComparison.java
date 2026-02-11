package io.github.chitralabs.sheetz.benchmarks.comparison;

import io.github.chitralabs.sheetz.benchmarks.model.Product;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * FastExcel: Write + Read in ~45 lines of actual code.
 * Cell-by-cell API — lightweight but verbose.
 */
public class FastExcelComparison {

    public static void main(String[] args) throws Exception {
        new File("output").mkdirs();
        List<Product> products = Product.sampleData();
        String filePath = "output/fastexcel_products.xlsx";

        // ── Write (cell-by-cell) ───────────────────────────
        try (OutputStream os = new FileOutputStream(filePath)) {
            Workbook wb = new Workbook(os, "Benchmark", "1.0");
            Worksheet ws = wb.newWorksheet("Products");

            // Header
            ws.value(0, 0, "Product Name");
            ws.value(0, 1, "Price");
            ws.value(0, 2, "In Stock");
            ws.value(0, 3, "Category");
            ws.value(0, 4, "Quantity");

            // Data rows
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                int row = i + 1;
                ws.value(row, 0, p.name);
                ws.value(row, 1, p.price);
                ws.value(row, 2, p.inStock.toString());
                ws.value(row, 3, p.category);
                ws.value(row, 4, p.quantity);
            }

            wb.finish();
        }

        // ── Read (cell-by-cell) ────────────────────────────
        List<Product> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {
            Sheet sheet = wb.getFirstSheet();
            try (Stream<Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(row -> {
                    Product p = new Product();
                    p.name = row.getCellText(0);
                    p.price = Double.parseDouble(row.getCellText(1));
                    p.inStock = Boolean.parseBoolean(row.getCellText(2));
                    p.category = row.getCellText(3);
                    p.quantity = (int) Double.parseDouble(row.getCellText(4));
                    result.add(p);
                });
            }
        }

        // ── Print ──────────────────────────────────────────
        System.out.println("=== FastExcel ===");
        System.out.println("Wrote " + products.size() + " products");
        System.out.println("Read back " + result.size() + " products:");
        result.forEach(p -> System.out.printf("  %-30s $%8.2f  inStock=%-5s  qty=%d%n",
                p.name, p.price, p.inStock, p.quantity));
    }
}
