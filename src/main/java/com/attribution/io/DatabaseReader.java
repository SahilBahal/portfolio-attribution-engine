package com.attribution.io;

import com.attribution.model.*;
import java.sql.*;
import java.util.*;

public class DatabaseReader {

    private static final String URL      = "jdbc:postgresql://localhost:5432/attribution_db";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "************";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<Holding> readHoldings(String period) throws SQLException {
        List<Holding> holdings = new ArrayList<>();
        String sql = """
            SELECT h.security, h.sector, h.weight, h.return_rate
            FROM holdings h
            JOIN portfolios p ON h.portfolio_id = p.id
            WHERE p.period = ?
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, period);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                holdings.add(new Holding(
                    rs.getString("security"),
                    rs.getString("sector"),
                    rs.getDouble("weight"),
                    rs.getDouble("return_rate")
                ));
            }
        }
        return holdings;
    }

    public List<BenchmarkSector> readBenchmark(String period) throws SQLException {
        List<BenchmarkSector> sectors = new ArrayList<>();
        String sql = "SELECT sector, weight, return_rate FROM benchmark_sectors WHERE period = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, period);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sectors.add(new BenchmarkSector(
                    rs.getString("sector"),
                    rs.getDouble("weight"),
                    rs.getDouble("return_rate")
                ));
            }
        }
        return sectors;
    }

    public List<String> getAvailablePeriods() throws SQLException {
        List<String> periods = new ArrayList<>();
        String sql = "SELECT period FROM portfolios GROUP BY period ORDER BY MIN(month_order)";

try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                periods.add(rs.getString("period"));
            }
        }
        return periods;
    }
}
