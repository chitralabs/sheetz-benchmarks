package io.github.chitralabs.sheetz.benchmarks.comparison;

import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.benchmarks.model.Product;

import java.io.File;
import java.util.List;

/**
 * Sheetz: Write + Read in ~15 lines of actual code.
 */
public class SheetzComparison {

    public static void main(String[] args) throws Exception {
        new File("output").mkdirs();
        List<Product> products = Product.sampleData();

        // ── Write ──────────────────────────────────────────
        Sheetz.write(products, "output/sheetz_products.xlsx");

        // ── Read ───────────────────────────────────────────
        List<Product> result = Sheetz.read("output/sheetz_products.xlsx", Product.class);

        // ── Print ──────────────────────────────────────────
        System.out.println("=== Sheetz ===");
        System.out.println("Wrote " + products.size() + " products");
        System.out.println("Read back " + result.size() + " products:");
        result.forEach(p -> System.out.printf("  %-30s $%8.2f  inStock=%-5s  qty=%d%n",
                p.name, p.price, p.inStock, p.quantity));
    }
}
