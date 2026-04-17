package com.attribution.io;

import com.attribution.model.*;
import java.io.*;
import java.util.*;

public class CsvReader {

    private static final String RESOURCES_PATH = "src/main/resources/";

    public List<Holding> readPortfolio(String filename) throws IOException {
        List<Holding> holdings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(RESOURCES_PATH + filename))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",");
                String security = parts[0].trim();
                String sector   = parts[1].trim();
                double weight   = Double.parseDouble(parts[2].trim());
                double ret      = Double.parseDouble(parts[3].trim());
                holdings.add(new Holding(security, sector, weight, ret));
            }
        }
        return holdings;
    }

    public List<BenchmarkSector> readBenchmark(String filename) throws IOException {
        List<BenchmarkSector> sectors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(RESOURCES_PATH + filename))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",");
                String sector = parts[0].trim();
                double weight = Double.parseDouble(parts[1].trim());
                double ret    = Double.parseDouble(parts[2].trim());
                sectors.add(new BenchmarkSector(sector, weight, ret));
            }
        }
        return sectors;
    }
}