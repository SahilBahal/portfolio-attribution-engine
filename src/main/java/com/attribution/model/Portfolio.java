package com.attribution.model;

import java.util.*;

public class Portfolio {
    private List<Holding> holdings;

    public Portfolio(List<Holding> holdings) {
        this.holdings = holdings;
    }

    public double calculateReturn() {
        return holdings.stream()
            .mapToDouble(h -> h.getWeight() * h.getReturnRate())
            .sum();
    }

    // Returns a map of sector -> [totalWeight, weightedReturnSum]
    public Map<String, double[]> aggregateBySector() {
        Map<String, double[]> sectorData = new LinkedHashMap<>();
        for (Holding h : holdings) {
            sectorData.putIfAbsent(h.getSector(), new double[]{0.0, 0.0});
            sectorData.get(h.getSector())[0] += h.getWeight();
            sectorData.get(h.getSector())[1] += h.getWeight() * h.getReturnRate();
        }
        return sectorData;
    }

    public List<Holding> getHoldings() { return holdings; }
}