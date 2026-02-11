package io.github.chitralabs.sheetz.benchmarks.jmh;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.poiji.bind.Poiji;
import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.benchmarks.model.Product;
import io.github.chitralabs.sheetz.benchmarks.model.ProductEasyExcel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ReadBenchmark {

    @Param({"1000", "10000", "100000"})
    private int rowCount;

    private String testFile;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        testFile = "benchmark_data/read_" + rowCount + ".xlsx";
        File f = new File(testFile);
        if (!f.exists()) {
            DataGenerator.generateTestFile(testFile, rowCount);
        }
    }

    @Benchmark
    public List<Product> sheetzRead() throws Exception {
        return Sheetz.read(testFile, Product.class);
    }

    @Benchmark
    public List<Product> poiRead() throws Exception {
        List<Product> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(testFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Product p = new Product();
                p.name = row.getCell(0).getStringCellValue();
                p.price = row.getCell(1).getNumericCellValue();
                p.inStock = row.getCell(2).getBooleanCellValue();
                p.category = row.getCell(3).getStringCellValue();
                p.quantity = (int) row.getCell(4).getNumericCellValue();
                result.add(p);
            }
        }
        return result;
    }

    @Benchmark
    public List<ProductEasyExcel> easyExcelRead() throws Exception {
        List<ProductEasyExcel> result = new ArrayList<>();
        EasyExcel.read(testFile, ProductEasyExcel.class, new ReadListener<ProductEasyExcel>() {
            @Override
            public void invoke(ProductEasyExcel data, AnalysisContext context) {
                result.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).sheet().doRead();
        return result;
    }

    @Benchmark
    public List<Product> fastExcelRead() throws Exception {
        List<Product> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(testFile);
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {
            org.dhatim.fastexcel.reader.Sheet sheet = wb.getFirstSheet();
            try (Stream<org.dhatim.fastexcel.reader.Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(row -> {
                    Product p = new Product();
                    p.name = row.getCellText(0);
                    p.price = Double.parseDouble(row.getCellText(1));
                    String inStockText = row.getCellText(2);
                    p.inStock = "1".equals(inStockText) || "true".equalsIgnoreCase(inStockText);
                    p.category = row.getCellText(3);
                    p.quantity = (int) Double.parseDouble(row.getCellText(4));
                    result.add(p);
                });
            }
        }
        return result;
    }

    @Benchmark
    public List<Product> poijiRead() throws Exception {
        return Poiji.fromExcel(new File(testFile), Product.class);
    }
}
