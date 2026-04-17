package com.attribution.engine;

import com.attribution.model.*;
import java.util.*;

public class AttributionCalculator {

    public AttributionResult calculate(Portfolio portfolio, Benchmark benchmark) {
        double portfolioReturn = portfolio.calculateReturn();
        double benchmarkReturn = benchmark.calculateReturn();
        double activeReturn    = portfolioReturn - benchmarkReturn;

        Map<String, double[]> portfolioSectors = portfolio.aggregateBySector();
        Map<String, BenchmarkSector> benchmarkMap = benchmark.toMap();

        Map<String, Double> allocationEffects = new LinkedHashMap<>();
        Map<String, Double> selectionEffects  = new LinkedHashMap<>();

        // Get all sector names from both portfolio and benchmark
        Set<String> allSectors = new LinkedHashSet<>();
        allSectors.addAll(portfolioSectors.keySet());
        allSectors.addAll(benchmarkMap.keySet());

        for (String sector : allSectors) {
            double portWeight = 0, portReturn = 0;
            double benchWeight = 0, benchReturn = 0;

            if (portfolioSectors.containsKey(sector)) {
                double[] data = portfolioSectors.get(sector);
                portWeight = data[0];
                portReturn = data[0] > 0 ? data[1] / data[0] : 0; // weighted avg return
            }
            if (benchmarkMap.containsKey(sector)) {
                benchWeight = benchmarkMap.get(sector).getWeight();
                benchReturn = benchmarkMap.get(sector).getReturnRate();
            }

            // Brinson allocation effect: (portWeight - benchWeight) * benchReturn
            double allocation = (portWeight - benchWeight) * benchReturn;

            // Brinson selection effect: portWeight * (portReturn - benchReturn)
            double selection = portWeight * (portReturn - benchReturn);

            allocationEffects.put(sector, allocation);
            selectionEffects.put(sector, selection);
        }

        return new AttributionResult(portfolioReturn, benchmarkReturn,
                                     activeReturn, allocationEffects, selectionEffects);
    }
}