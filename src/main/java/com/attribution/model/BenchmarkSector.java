package com.attribution.model;

public class BenchmarkSector {
    private String sector;
    private double weight;
    private double returnRate;

    public BenchmarkSector(String sector, double weight, double returnRate) {
        this.sector = sector;
        this.weight = weight;
        this.returnRate = returnRate;
    }

    public String getSector()     { return sector; }
    public double getWeight()     { return weight; }
    public double getReturnRate() { return returnRate; }
}