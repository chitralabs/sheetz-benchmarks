# Sheetz Benchmarks — Java Excel Library Comparison: Code Simplicity & Performance

[![Build](https://github.com/chitralabs/sheetz-benchmarks/actions/workflows/ci.yml/badge.svg)](https://github.com/chitralabs/sheetz-benchmarks/actions/workflows/ci.yml)
[![Java 11+](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![JMH](https://img.shields.io/badge/JMH-1.37-orange.svg)](https://openjdk.org/projects/code-tools/jmh/)
[![GitHub stars](https://img.shields.io/github/stars/chitralabs/sheetz?style=social)](https://github.com/chitralabs/sheetz)

**How does [Sheetz](https://github.com/chitralabs/sheetz) compare to Apache POI, EasyExcel, FastExcel, and Poiji?** This project answers that question with side-by-side code comparisons and reproducible JMH performance benchmarks across 1K, 10K, and 100K rows.

### TL;DR

- Sheetz needs **1 line** to read or write Excel. Apache POI needs **25+**. EasyExcel needs **12+** (with listener).
- At 100K rows, Sheetz writes are **5.8x faster than Apache POI** thanks to automatic SXSSF streaming.
- FastExcel and EasyExcel have faster raw throughput, but require significantly more code and lack features like validation, multi-format support, and automatic type conversion.

> **New to Sheetz?** See the [main repo](https://github.com/chitralabs/sheetz) for docs and API reference, or browse [8 runnable examples](https://github.com/chitralabs/sheetz-examples).

---

## Libraries Compared

| Library | Version | API Style | Read | Write | Annotations | Validation |
|---------|---------|-----------|:----:|:-----:|:-----------:|:----------:|
| [**Sheetz**](https://github.com/chitralabs/sheetz) | 1.0.1 | One-liner, annotation-based | Yes | Yes | `@Column` | Built-in |
| [Apache POI](https://poi.apache.org/) | 5.2.5 | Manual cell-level API | Yes | Yes | None | None |
| [EasyExcel](https://github.com/alibaba/easyexcel) | 4.0.3 | Annotation + listener pattern | Yes | Yes | `@ExcelProperty` | None |
| [FastExcel](https://github.com/dhatim/fastexcel) | 0.19.0 | Lightweight cell-level API | Yes | Yes | None | None |
| [Poiji](https://github.com/ozlerhakan/poiji) | 5.2.0 | Annotation-based (read-only) | Yes | No | `@ExcelCellName` | None |

---

## Code Comparison

The question: **How many lines does it take to write 10 products to Excel and read them back?**

| Library | Write | Read | Total Lines | Notes |
|---------|------:|-----:|------------:|-------|
| **Sheetz** | **1** | **1** | **~15** | One-liner read & write |
| Apache POI | ~25 | ~20 | ~60 | Manual workbook/sheet/row/cell |
| EasyExcel | ~3 | ~12 | ~30 | Separate model + listener pattern |
| FastExcel | ~18 | ~15 | ~45 | Cell-by-cell positional API |
| Poiji | — | ~1 | ~15 | Read-only library |

### Sheetz — 1 line write, 1 line read

```java
Sheetz.write(products, "products.xlsx");
List<Product> result = Sheetz.read("products.xlsx", Product.class);
```

That's the entire implementation. No workbook objects, no cell iteration, no type casting.

### Apache POI — ~25 lines write, ~20 lines read

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

### EasyExcel — 3 lines write, ~12 lines read (listener pattern)

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

### FastExcel — ~18 lines write, ~15 lines read

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

[View all comparison source code](src/main/java/io/github/chitralabs/sheetz/benchmarks/comparison/)

---

## Performance Benchmarks (JMH)

All benchmarks measure **average time per operation** (lower is better) using [JMH 1.37](https://openjdk.org/projects/code-tools/jmh/).

> **Environment:** JDK 11.0.30 (OpenJDK), Apple Silicon (macOS), 2 forks, 3 warmup iterations, 5 measurement iterations

### Write Performance (ms/op — lower is better)

| Library | 1K rows | 10K rows | 100K rows |
|---------|--------:|---------:|----------:|
| FastExcel | 6.48 ± 1.47 | 31.95 ± 1.00 | 309.70 ± 17.59 |
| EasyExcel | 11.44 ± 0.50 | 58.60 ± 1.54 | 542.84 ± 33.32 |
| **Sheetz** | **23.15 ± 0.62** | **232.51 ± 18.70** | **423.75 ± 20.14** |
| Apache POI | 22.46 ± 0.53 | 217.17 ± 9.59 | 2,453.35 ± 112.24 |

At small file sizes, Sheetz and Apache POI are comparable. **At 100K rows, Sheetz is 5.8x faster than POI** because it automatically switches to SXSSF streaming — something POI requires you to configure manually.

### Read Performance (ms/op — lower is better)

| Library | 1K rows | 10K rows | 100K rows |
|---------|--------:|---------:|----------:|
| FastExcel | 2.43 ± 0.12 | 24.88 ± 1.75 | 210.17 ± 4.10 |
| EasyExcel | 4.91 ± 0.55 | 42.66 ± 6.87 | 334.17 ± 15.53 |
| Apache POI | 10.86 ± 0.46 | 106.02 ± 9.11 | 1,097.20 ± 79.91 |
| Poiji | 12.26 ± 0.40 | 114.92 ± 1.97 | 1,042.16 ± 50.28 |
| **Sheetz** | **13.18 ± 0.58** | **128.35 ± 12.95** | **1,285.89 ± 64.96** |

For reads, FastExcel and EasyExcel are faster at raw throughput. Sheetz performs comparably to Apache POI and Poiji while offering annotation-based mapping, automatic type conversion, and built-in validation that those libraries don't provide.

### The Tradeoff

Sheetz prioritizes **developer experience** — 1 line of code, automatic type conversion, built-in validation, multi-format support. Libraries like FastExcel and EasyExcel win on raw speed but require more code and offer fewer features out of the box.

Choose based on your priority:
- **Fastest throughput →** FastExcel or EasyExcel
- **Fewest lines of code + most features →** Sheetz
- **Already using POI and want a drop-in wrapper →** Sheetz (it uses POI internally)

> Full raw JMH output is available in [`results/results.txt`](results/results.txt).

---

## How to Run

### Prerequisites

- Java 11+
- Maven 3.6+

### Code Comparison Demos

Each comparison class has a `main()` that writes products to Excel and reads them back, showing the actual code needed for each library.

```bash
# Clone and compile
git clone https://github.com/chitralabs/sheetz-benchmarks.git
cd sheetz-benchmarks
mvn compile

# Run any comparison
mvn exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.SheetzComparison"
mvn exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.ApachePoiComparison"
mvn exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.EasyExcelComparison"
mvn exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.FastExcelComparison"
mvn exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.benchmarks.comparison.PoijiComparison"
```

### JMH Performance Benchmarks

```bash
# Build the benchmark JAR
mvn clean package

# Run all benchmarks (full run — takes ~30 minutes)
java -jar target/benchmarks.jar

# Quick smoke test (~2 minutes)
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1

# Run only read or write benchmarks
java -jar target/benchmarks.jar ReadBenchmark
java -jar target/benchmarks.jar WriteBenchmark

# Run with a specific row count
java -jar target/benchmarks.jar -p rowCount=10000
```

---

## Methodology

| Setting | Value |
|---------|-------|
| **Framework** | [JMH 1.37](https://openjdk.org/projects/code-tools/jmh/) (OpenJDK Microbenchmark Harness) |
| **Mode** | Average time per operation (`Mode.AverageTime`) |
| **Forks** | 2 (separate JVM processes) |
| **Warmup** | 3 iterations, 1 second each |
| **Measurement** | 5 iterations, 1 second each |
| **Row counts** | 1,000 / 10,000 / 100,000 (via `@Param`) |
| **Data** | Fixed random seed for reproducibility |
| **File format** | `.xlsx` (OOXML) for all libraries |
| **Fairness** | All libraries read/write identical data; test files generated once in `@Setup` |

Results will vary by JVM version, OS, and hardware. Run the benchmarks yourself to get numbers for your environment.

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

- [Sheetz — main library](https://github.com/chitralabs/sheetz)
- [Sheetz Examples — 8 runnable demos](https://github.com/chitralabs/sheetz-examples)
- [Sheetz on Maven Central](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core)

## License

[Apache License 2.0](LICENSE) — free for commercial and personal use.

---

If these benchmarks helped you evaluate Sheetz, consider giving the [main repo](https://github.com/chitralabs/sheetz) a star. It helps other developers discover the project.

[![Star Sheetz](https://img.shields.io/github/stars/chitralabs/sheetz?style=social)](https://github.com/chitralabs/sheetz)
