package io.github.chitralabs.sheetz.benchmarks.jmh;

import com.alibaba.excel.EasyExcel;
import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.benchmarks.model.Product;
import io.github.chitralabs.sheetz.benchmarks.model.ProductEasyExcel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class WriteBenchmark {

    @Param({"1000", "10000", "100000"})
    private int rowCount;

    private List<Product> products;
    private List<ProductEasyExcel> productsEasyExcel;

    @Setup(Level.Trial)
    public void setup() {
        new File("benchmark_data").mkdirs();
        products = DataGenerator.generateProducts(rowCount);
        productsEasyExcel = DataGenerator.toEasyExcel(products);
    }

    @Benchmark
    public void sheetzWrite() throws Exception {
        Sheetz.write(products, "benchmark_data/bench_sheetz.xlsx");
    }

    @Benchmark
    public void poiWrite() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Product Name");
            header.createCell(1).setCellValue("Price");
            header.createCell(2).setCellValue("In Stock");
            header.createCell(3).setCellValue("Category");
            header.createCell(4).setCellValue("Quantity");

            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(p.name);
                row.createCell(1).setCellValue(p.price);
                row.createCell(2).setCellValue(p.inStock);
                row.createCell(3).setCellValue(p.category);
                row.createCell(4).setCellValue(p.quantity);
            }

            try (FileOutputStream fos = new FileOutputStream("benchmark_data/bench_poi.xlsx")) {
                workbook.write(fos);
            }
        }
    }

    @Benchmark
    public void easyExcelWrite() throws Exception {
        EasyExcel.write("benchmark_data/bench_easyexcel.xlsx", ProductEasyExcel.class)
                .sheet("Products")
                .doWrite(productsEasyExcel);
    }

    @Benchmark
    public void fastExcelWrite() throws Exception {
        try (OutputStream os = new FileOutputStream("benchmark_data/bench_fastexcel.xlsx")) {
            Workbook wb = new Workbook(os, "Benchmark", "1.0");
            Worksheet ws = wb.newWorksheet("Products");

            ws.value(0, 0, "Product Name");
            ws.value(0, 1, "Price");
            ws.value(0, 2, "In Stock");
            ws.value(0, 3, "Category");
            ws.value(0, 4, "Quantity");

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
    }
}
