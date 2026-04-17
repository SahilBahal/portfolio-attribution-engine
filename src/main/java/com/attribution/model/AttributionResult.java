package com.attribution.model;

import java.util.Map;

public class AttributionResult {
    private double portfolioReturn;
    private double benchmarkReturn;
    private double activeReturn;
    private Map<String, Double> allocationEffects;
    private Map<String, Double> selectionEffects;

    public AttributionResult(double portfolioReturn, double benchmarkReturn,
                             double activeReturn,
                             Map<String, Double> allocationEffects,
                             Map<String, Double> selectionEffects) {
        this.portfolioReturn   = portfolioReturn;
        this.benchmarkReturn   = benchmarkReturn;
        this.activeReturn      = activeReturn;
        this.allocationEffects = allocationEffects;
        this.selectionEffects  = selectionEffects;
    }

    public double getPortfolioReturn()               { return portfolioReturn; }
    public double getBenchmarkReturn()               { return benchmarkReturn; }
    public double getActiveReturn()                  { return activeReturn; }
    public Map<String, Double> getAllocationEffects() { return allocationEffects; }
    public Map<String, Double> getSelectionEffects()  { return selectionEffects; }
}