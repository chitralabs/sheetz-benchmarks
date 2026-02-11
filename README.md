# Sheetz Benchmarks

[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)

**Side-by-side comparison of [Sheetz](https://github.com/chitralabs/sheetz) vs the 4 major Java Excel libraries** — code simplicity and JMH performance benchmarks.

| Library | Version | Type |
|---------|---------|------|
| [Sheetz](https://github.com/chitralabs/sheetz) | 1.0.1 | Annotation-based, zero boilerplate |
| [Apache POI](https://poi.apache.org/) | 5.2.5 | Manual cell-level API |
| [EasyExcel](https://github.com/alibaba/easyexcel) | 4.0.3 | Annotation-based, listener pattern |
| [FastExcel](https://github.com/dhatim/fastexcel) | 0.19.0 | Lightweight cell-level API |
| [Poiji](https://github.com/ozlerhakan/poiji) | 5.2.0 | Read-only, annotation-based |

---

## Code Comparison

How many lines does it take to **write 10 products to Excel and read them back**?

| Library | Write | Read | Total | Notes |
|---------|------:|-----:|------:|-------|
| **Sheetz** | **1** | **1** | **~15** | One-liner read & write |
| Apache POI | ~25 | ~20 | **~60** | Manual workbook/sheet/row/cell |
| EasyExcel | ~3 | ~12 | **~30** | Separate model + listener pattern |
| FastExcel | ~18 | ~15 | **~45** | Cell-by-cell API |
| Poiji | N/A | ~1 | **~15** | Read-only library |

### Sheetz (1 line write, 1 line read)

```java
// Write
Sheetz.write(products, "products.xlsx");

// Read
List<Product> result = Sheetz.read("products.xlsx", Product.class);
```

### Apache POI (~25 lines write, ~20 lines read)

```java
// Write — manual workbook creation
try (Workbook workbook = new XSSFWorkbook()) {
    Sheet sheet = workbook.createSheet("Products");
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Product Name");
    headerRow.createCell(1).setCellValue("Price");
    // ... create each header cell manually

    for (int i = 0; i < products.size(); i++) {
        Row row = sheet.createRow(i + 1);
        row.createCell(0).setCellValue(products.get(i).name);
        row.createCell(1).setCellValue(products.get(i).price);
        // ... set each cell manually
    }
    workbook.write(new FileOutputStream("products.xlsx"));
}

// Read — manual row/cell iteration
try (Workbook workbook = new XSSFWorkbook(new FileInputStream("products.xlsx"))) {
    Sheet sheet = workbook.getSheetAt(0);
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        Product p = new Product();
        p.name = row.getCell(0).getStringCellValue();
        p.price = row.getCell(1).getNumericCellValue();
        // ... extract each field manually
    }
}
```

### EasyExcel (3 lines write, ~12 lines read with listener)

```java
// Write
EasyExcel.write("products.xlsx", ProductEasyExcel.class)
        .sheet("Products").doWrite(data);

// Read — requires listener pattern
List<ProductEasyExcel> result = new ArrayList<>();
EasyExcel.read("products.xlsx", ProductEasyExcel.class,
    new ReadListener<ProductEasyExcel>() {
        public void invoke(ProductEasyExcel data, AnalysisContext ctx) {
            result.add(data);
        }
        public void doAfterAllAnalysed(AnalysisContext ctx) {}
    }).sheet().doRead();
```

### FastExcel (~18 lines write, ~15 lines read)

```java
// Write — cell by cell
Workbook wb = new Workbook(outputStream, "App", "1.0");
Worksheet ws = wb.newWorksheet("Products");
ws.value(0, 0, "Product Name");
ws.value(0, 1, "Price");
// ... set each header and data cell by position

// Read — cell by cell
ReadableWorkbook wb = new ReadableWorkbook(inputStream);
Sheet sheet = wb.getFirstSheet();
sheet.openStream().skip(1).forEach(row -> {
    Product p = new Product();
    p.name = row.getCellText(0);
    p.price = Double.parseDouble(row.getCellText(1));
    // ... parse each cell manually
});
```

---

## Performance Benchmarks (JMH)

Benchmarks measure **average time per operation** (lower is better) using [JMH](https://openjdk.org/projects/code-tools/jmh/).

> JDK 11.0.30 (OpenJDK), Apple Silicon, 2 forks / 3 warmup / 5 measurement iterations

### Read Performance (ms/op, lower is better)

| Library | 1K rows | 10K rows | 100K rows |
|---------|--------:|---------:|----------:|
| FastExcel | 2.43 ± 0.12 | 24.88 ± 1.75 | 210.17 ± 4.10 |
| EasyExcel | 4.91 ± 0.55 | 42.66 ± 6.87 | 334.17 ± 15.53 |
| Apache POI | 10.86 ± 0.46 | 106.02 ± 9.11 | 1,097.20 ± 79.91 |
| Poiji | 12.26 ± 0.40 | 114.92 ± 1.97 | 1,042.16 ± 50.28 |
| **Sheetz** | 13.18 ± 0.58 | 128.35 ± 12.95 | 1,285.89 ± 64.96 |

### Write Performance (ms/op, lower is better)

| Library | 1K rows | 10K rows | 100K rows |
|---------|--------:|---------:|----------:|
| FastExcel | 6.48 ± 1.47 | 31.95 ± 1.00 | 309.70 ± 17.59 |
| EasyExcel | 11.44 ± 0.50 | 58.60 ± 1.54 | 542.84 ± 33.32 |
| **Sheetz** | 23.15 ± 0.62 | 232.51 ± 18.70 | **423.75 ± 20.14** |
| Apache POI | 22.46 ± 0.53 | 217.17 ± 9.59 | 2,453.35 ± 112.24 |

**Key takeaway:** Sheetz trades a small overhead for annotation-based convenience. At 100K rows, Sheetz writes are **5.8x faster than raw Apache POI** thanks to automatic streaming — while requiring only 1 line of code vs ~25.

> Results vary by JVM, OS, and hardware. Run `java -jar target/benchmarks.jar` to benchmark on your machine.

---

## How to Run

### Prerequisites

- Java 11+
- Maven 3.6+

### Comparison Examples

Run any comparison demo:

```bash
# Sheetz
mvn compile exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.SheetzComparison"

# Apache POI
mvn compile exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.ApachePoiComparison"

# EasyExcel
mvn compile exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.EasyExcelComparison"

# FastExcel
mvn compile exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.FastExcelComparison"

# Poiji
mvn compile exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.PoijiComparison"
```

### JMH Benchmarks

```bash
# Build the benchmark JAR
mvn clean package

# Run all benchmarks (full run — takes ~30 minutes)
java -jar target/benchmarks.jar

# Quick smoke test (1 fork, 1 warmup iteration, 1 measurement iteration)
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1

# Run only read benchmarks
java -jar target/benchmarks.jar ReadBenchmark

# Run only write benchmarks
java -jar target/benchmarks.jar WriteBenchmark

# Run with specific row count
java -jar target/benchmarks.jar -p rowCount=1000
```

---

## Methodology

- **Framework**: [JMH 1.37](https://openjdk.org/projects/code-tools/jmh/) (OpenJDK Microbenchmark Harness)
- **Mode**: Average time per operation (`Mode.AverageTime`)
- **Default config**: 2 forks, 3 warmup iterations, 5 measurement iterations
- **Data**: Generated with fixed random seed for reproducibility
- **File format**: `.xlsx` (OOXML) for all libraries
- **Fairness**: All libraries read/write identical data; test files generated once in `@Setup`

---

## Project Structure

```
src/main/java/io/github/chitralabs/sheetz/benchmarks/
├── comparison/          — Side-by-side code demos (each has main())
│   ├── SheetzComparison.java
│   ├── ApachePoiComparison.java
│   ├── EasyExcelComparison.java
│   ├── FastExcelComparison.java
│   └── PoijiComparison.java
├── jmh/                 — JMH performance benchmarks
│   ├── ReadBenchmark.java
│   ├── WriteBenchmark.java
│   └── DataGenerator.java
└── model/               — Shared data models
    ├── Product.java            (Sheetz + Poiji annotations)
    ├── ProductEasyExcel.java   (EasyExcel annotations)
    └── ProductPoi.java         (Plain POJO for manual APIs)
```

---

## Links

- [Sheetz on GitHub](https://github.com/chitralabs/sheetz)
- [Sheetz on Maven Central](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core)

---

## License

Apache License 2.0
