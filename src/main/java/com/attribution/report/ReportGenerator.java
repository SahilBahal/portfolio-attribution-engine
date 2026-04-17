package com.attribution.report;

import com.attribution.model.AttributionResult;
import java.util.*;

public class ReportGenerator {

    public void printReport(AttributionResult result) {
        System.out.println("===========================================");
        System.out.println("   PORTFOLIO ATTRIBUTION REPORT");
        System.out.println("===========================================");
        System.out.printf("Portfolio Return : %6.2f%%%n", result.getPortfolioReturn() * 100);
        System.out.printf("Benchmark Return : %6.2f%%%n", result.getBenchmarkReturn() * 100);
        System.out.printf("Active Return    : %6.2f%%%n", result.getActiveReturn() * 100);
        System.out.println("-------------------------------------------");
        System.out.println("Sector Attribution:");
        System.out.println();
        System.out.printf("  %-15s %12s %12s%n", "Sector", "Allocation", "Selection");
        System.out.printf("  %-15s %12s %12s%n", "------", "----------", "---------");

        Set<String> sectors = result.getAllocationEffects().keySet();
        for (String sector : sectors) {
            double alloc = result.getAllocationEffects().get(sector) * 100;
            double sel   = result.getSelectionEffects().get(sector) * 100;
            System.out.printf("  %-15s %+11.2f%% %+11.2f%%%n", sector, alloc, sel);
        }
        System.out.println("===========================================");
    }
}