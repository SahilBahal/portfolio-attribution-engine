package com.attribution.model;

import java.util.*;

public class Benchmark {
    private List<BenchmarkSector> sectors;

    public Benchmark(List<BenchmarkSector> sectors) {
        this.sectors = sectors;
    }

    public double calculateReturn() {
        return sectors.stream()
            .mapToDouble(s -> s.getWeight() * s.getReturnRate())
            .sum();
    }

    public Map<String, BenchmarkSector> toMap() {
        Map<String, BenchmarkSector> map = new LinkedHashMap<>();
        for (BenchmarkSector s : sectors) {
            map.put(s.getSector(), s);
        }
        return map;
    }

    public List<BenchmarkSector> getSectors() { return sectors; }
}