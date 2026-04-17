package com.attribution;

import com.attribution.engine.AttributionCalculator;
import com.attribution.io.DatabaseReader;
import com.attribution.model.*;
import com.attribution.report.ReportGenerator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        DatabaseReader reader = new DatabaseReader();
        AttributionCalculator calculator = new AttributionCalculator();
        ReportGenerator reporter = new ReportGenerator();

        List<String> periods = reader.getAvailablePeriods();

        for (String period : periods) {
            System.out.println("\n\n===========================================");
            System.out.println("         PERIOD: " + period);
            System.out.println("===========================================");

            List<Holding> holdings = reader.readHoldings(period);
            List<BenchmarkSector> benchmarkSectors = reader.readBenchmark(period);

            Portfolio portfolio = new Portfolio(holdings);
            Benchmark benchmark = new Benchmark(benchmarkSectors);

            AttributionResult result = calculator.calculate(portfolio, benchmark);
            reporter.printReport(result);
        }
    }
}