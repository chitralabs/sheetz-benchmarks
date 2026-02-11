package io.github.chitralabs.sheetz.benchmarks.comparison;

import io.github.chitralabs.sheetz.benchmarks.model.Product;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Apache POI: Write + Read in ~60 lines of actual code.
 * Requires manual workbook/sheet/row/cell creation and iteration.
 */
public class ApachePoiComparison {

    public static void main(String[] args) throws Exception {
        new File("output").mkdirs();
        List<Product> products = Product.sampleData();
        String filePath = "output/poi_products.xlsx";

        // ── Write ──────────────────────────────────────────
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Product Name");
            headerRow.createCell(1).setCellValue("Price");
            headerRow.createCell(2).setCellValue("In Stock");
            headerRow.createCell(3).setCellValue("Category");
            headerRow.createCell(4).setCellValue("Quantity");

            // Create data rows
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(p.name);
                row.createCell(1).setCellValue(p.price);
                row.createCell(2).setCellValue(p.inStock);
                row.createCell(3).setCellValue(p.category);
                row.createCell(4).setCellValue(p.quantity);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }

        // ── Read ───────────────────────────────────────────
        List<Product> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Product p = new Product();
                p.name = getStringValue(row.getCell(0));
                p.price = row.getCell(1).getNumericCellValue();
                p.inStock = row.getCell(2).getBooleanCellValue();
                p.category = getStringValue(row.getCell(3));
                p.quantity = (int) row.getCell(4).getNumericCellValue();
                result.add(p);
            }
        }

        // ── Print ──────────────────────────────────────────
        System.out.println("=== Apache POI ===");
        System.out.println("Wrote " + products.size() + " products");
        System.out.println("Read back " + result.size() + " products:");
        result.forEach(p -> System.out.printf("  %-30s $%8.2f  inStock=%-5s  qty=%d%n",
                p.name, p.price, p.inStock, p.quantity));
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        return "";
    }
}
