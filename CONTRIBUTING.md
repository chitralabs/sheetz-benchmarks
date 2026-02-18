# Contributing to Sheetz Benchmarks

Thank you for your interest in contributing! Please see the [main Sheetz contribution guidelines](https://github.com/chitralabs/sheetz/blob/main/CONTRIBUTING.md) for the full guide.

## Quick Start

```bash
git clone https://github.com/chitralabs/sheetz-benchmarks.git
cd sheetz-benchmarks
mvn clean compile
```

## Adding a New Library Comparison

1. Add the library dependency to `pom.xml`
2. Create a comparison class in `src/main/java/.../comparison/`
3. Add JMH benchmarks in `src/main/java/.../jmh/`
4. Update the benchmark results in `README.md`

## Running Benchmarks

```bash
mvn clean package
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1  # Quick smoke test
```

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
