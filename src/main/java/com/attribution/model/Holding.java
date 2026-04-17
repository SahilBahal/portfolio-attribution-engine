package com.attribution.model;

public class Holding {
    private String security;
    private String sector;
    private double weight;
    private double returnRate;

    public Holding(String security, String sector, double weight, double returnRate) {
        this.security = security;
        this.sector = sector;
        this.weight = weight;
        this.returnRate = returnRate;
    }

    public String getSecurity()   { return security; }
    public String getSector()     { return sector; }
    public double getWeight()     { return weight; }
    public double getReturnRate() { return returnRate; }
}