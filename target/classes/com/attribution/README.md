# Portfolio Attribution Engine

A Java application that connects to a PostgreSQL database, queries portfolio and benchmark data across a full calendar year, and breaks down monthly performance using Brinson style sector attribution.

Built from scratch to understand the kind of work done on attribution calculation and reporting platforms in investment technology.

---

## What It Does

Every actively managed portfolio is measured against a benchmark. An index fund can match 
the market mechanically — no decisions required. The entire justification for active 
management, and the fees that come with it, is the belief that skilled decision-making can 
consistently do better. But "we beat the benchmark by 1.15%" is not an answer. It's a 
starting point.

The real question is *which decisions deserve the credit*. 
A portfolio manager makes two fundamentally different kinds of calls. The first is 
allocation: how much of the portfolio to put into each sector relative to what the benchmark 
holds. Overweight Technology, underweight Energy, skip Consumer entirely, these are 
deliberate bets that certain parts of the market will outperform. The second is selection: 
within a given sector, which specific securities to hold. You might have the right amount 
of Healthcare exposure, but if you picked the wrong stocks inside that sector, the 
allocation call goes unrewarded.

These two effects can work together, work against each other, or move completely 
independently. A manager can make a brilliant allocation call by correctly overweighting a 
sector that runs up, and still generate negative selection effect because their individual 
picks inside that sector lagged. Conversely, exceptional stock picking within a sector can 
rescue a poor allocation decision. Without decomposing performance into these two components, 
you have no idea which skill is actually driving returns, which means you have no idea 
whether this year's outperformance will repeat or was just fortunate timing.

That decomposition is what attribution does. It takes the gap between portfolio return and 
benchmark return and assigns every basis point of it to a specific decision, in a specific 
sector, in a specific period. It turns a single number into a full accounting of where 
value was created and where it was left on the table.

This engine answers that question for every month of the year. It pulls structured data from a relational database, computes portfolio and benchmark returns, and decomposes the difference into two distinct effects — allocation and selection — broken out by sector.

---

## The Math

### Portfolio Return
Weighted average of individual security returns across all holdings:

```
Portfolio Return = Σ (weight_i × return_i)
```

### Benchmark Return
Weighted average of benchmark sector returns:

```
Benchmark Return = Σ (benchmarkWeight_i × benchmarkReturn_i)
```

### Active Return
The difference — how much the portfolio over or underperformed:

```
Active Return = Portfolio Return − Benchmark Return
```

### Brinson Attribution

This is the core of the engine. Active return gets split into two components per sector, based on the Brinson-Hood-Beebower model (1986), which remains the industry standard for performance attribution.

**Allocation Effect**

Measures whether the decision to overweight or underweight a sector relative to the benchmark added value:

```
Allocation = (Portfolio Weight − Benchmark Weight) × Benchmark Return
```

A positive number means overweighting that sector was a good call. A negative number means the tilt hurt performance.

**Selection Effect**

Measures whether the securities chosen within a sector outperformed the benchmark's equivalent sector exposure:

```
Selection = Portfolio Weight × (Portfolio Sector Return − Benchmark Sector Return)
```

A positive number means the individual security picks within that sector were better than what the benchmark held. A negative number means the benchmark's sector exposure would have served you better.

The sum of allocation and selection effects across all sectors reconciles back to the total active return.

---

## Database Schema

Data lives in a PostgreSQL database called `attribution_db` with three tables:

```
portfolios
├── id            SERIAL PRIMARY KEY
├── name          VARCHAR        -- portfolio name
├── period        VARCHAR        -- e.g. 'January', 'February'
└── month_order   INT            -- ensures calendar-order sorting

holdings
├── id            SERIAL PRIMARY KEY
├── portfolio_id  INT → portfolios.id
├── security      VARCHAR        -- ticker e.g. 'AAPL', 'MSFT'
├── sector        VARCHAR        -- e.g. 'Technology', 'Financials'
├── weight        DECIMAL
└── return_rate   DECIMAL

benchmark_sectors
├── id            SERIAL PRIMARY KEY
├── period        VARCHAR
├── sector        VARCHAR
├── weight        DECIMAL
└── return_rate   DECIMAL
```

Holdings are linked to portfolios via a foreign key. The `month_order` column on `portfolios` ensures the application retrieves periods in January–December order rather than alphabetical order, which PostgreSQL would otherwise default to.

---

## Project Structure

```
attribution-engine/
├── pom.xml
└── src/
    └── main/
        ├── java/com/attribution/
        │   ├── model/
        │   │   ├── Holding.java               # One portfolio security
        │   │   ├── BenchmarkSector.java        # One benchmark sector entry
        │   │   ├── Portfolio.java              # Holdings collection + sector aggregation
        │   │   ├── Benchmark.java              # Benchmark sectors collection
        │   │   └── AttributionResult.java      # Stores all computed results
        │   ├── engine/
        │   │   └── AttributionCalculator.java  # Brinson attribution logic
        │   ├── io/
        │   │   └── DatabaseReader.java         # JDBC queries against PostgreSQL
        │   ├── report/
        │   │   └── ReportGenerator.java        # Formats and prints the report
        │   └── Main.java                       # Entry point, loops over all periods
        └── resources/
            ├── portfolio.csv
            └── benchmark.csv
```

##  Output

```
===========================================
         PERIOD: January
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   4.75%
Benchmark Return :   4.10%
Active Return    :   0.65%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.70%       +0.05%
  Financials            -0.20%       -0.15%
  Energy                -0.15%       +0.20%
  Healthcare            +0.10%       +0.30%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: February
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   5.65%
Benchmark Return :   4.20%
Active Return    :   1.45%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.80%       +0.00%
  Financials            +0.00%       +0.40%
  Energy                -0.10%       +0.10%
  Healthcare            +0.10%       +0.25%
  Consumer              -0.10%       -0.00%
===========================================


===========================================
         PERIOD: March
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   4.95%
Benchmark Return :   4.10%
Active Return    :   0.85%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.60%       +0.05%
  Financials            -0.15%       -0.15%
  Energy                +0.00%       +0.30%
  Healthcare            +0.15%       +0.25%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: April
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   5.66%
Benchmark Return :   4.65%
Active Return    :   1.01%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.80%       +0.11%
  Financials            -0.08%       +0.00%
  Energy                -0.09%       +0.12%
  Healthcare            +0.10%       +0.25%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: May
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   6.70%
Benchmark Return :   4.75%
Active Return    :   1.95%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.90%       +0.45%
  Financials            +0.00%       +0.20%
  Energy                -0.10%       +0.00%
  Healthcare            +0.10%       +0.50%
  Consumer              -0.10%       -0.00%
===========================================


===========================================
         PERIOD: June
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   4.05%
Benchmark Return :   3.40%
Active Return    :   0.65%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.50%       +0.25%
  Financials            -0.15%       +0.00%
  Energy                +0.00%       +0.15%
  Healthcare            +0.10%       +0.00%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: July
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   6.04%
Benchmark Return :   4.65%
Active Return    :   1.39%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.70%       +0.46%
  Financials            -0.10%       +0.18%
  Energy                -0.12%       +0.12%
  Healthcare            +0.10%       +0.25%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: August
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   4.45%
Benchmark Return :   3.50%
Active Return    :   0.95%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.50%       +0.05%
  Financials            +0.00%       +0.00%
  Energy                -0.15%       +0.00%
  Healthcare            +0.15%       +0.50%
  Consumer              -0.10%       -0.00%
===========================================


===========================================
         PERIOD: September
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   5.40%
Benchmark Return :   4.60%
Active Return    :   0.80%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.70%       +0.25%
  Financials            -0.20%       +0.00%
  Energy                +0.00%       +0.15%
  Healthcare            +0.10%       +0.00%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: October
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   6.50%
Benchmark Return :   4.85%
Active Return    :   1.65%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.80%       +0.60%
  Financials            +0.00%       +0.20%
  Energy                -0.15%       +0.00%
  Healthcare            +0.15%       +0.25%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: November
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   5.68%
Benchmark Return :   4.30%
Active Return    :   1.38%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.70%       +0.40%
  Financials            -0.08%       +0.18%
  Energy                -0.09%       +0.12%
  Healthcare            +0.10%       +0.25%
  Consumer              -0.20%       -0.00%
===========================================


===========================================
         PERIOD: December
===========================================
===========================================
   PORTFOLIO ATTRIBUTION REPORT
===========================================
Portfolio Return :   6.31%
Benchmark Return :   5.20%
Active Return    :   1.11%
-------------------------------------------
Sector Attribution:

  Sector            Allocation    Selection
  ------            ----------    ---------
  Technology            +0.80%       +0.11%
  Financials            +0.00%       +0.20%
  Energy                -0.20%       +0.00%
  Healthcare            +0.15%       +0.25%
  Consumer              -0.20%       -0.00%
===========================================
```

Runs for all 12 months in calendar order, one report per period.


---

## How to Run

### Prerequisites

- Java JDK 17+
- PostgreSQL 14+ with a database called `attribution_db`
- VS Code with the **Extension Pack for Java** installed
- Maven 3.9+

### Database Setup

Connect to your PostgreSQL instance and run the schema and seed scripts to create the three tables and populate 12 months of portfolio and benchmark data.

### Configuration

Open `src/main/java/com/attribution/io/DatabaseReader.java` and update the connection constants:

```java
private static final String URL      = "jdbc:postgresql://localhost:5432/attribution_db";
private static final String USER     = "postgres";
private static final String PASSWORD = "your_password_here";
```

### Running

Open `Main.java` in VS Code and click **Run Java** above the `main` method. All 12 monthly reports print to the terminal in calendar order.

---

## Design Decisions

**Why keep model classes separate from database logic?**

`DatabaseReader` is the only class that knows PostgreSQL exists. Everything else — `Portfolio`, `Benchmark`, `AttributionCalculator` — operates on plain Java objects. This means the data source can be swapped without touching any calculation or reporting code. The original CSV reader from v1 still works independently for the same reason.

**Why `Map<String, double[]>` for sector aggregation in Portfolio?**

When rolling up individual holdings to the sector level, two numbers need to travel together: total sector weight and the sum of weighted returns. A two-element array communicates this directly at the call site without creating a throwaway intermediate class for a single method.

**Why `LinkedHashMap` instead of `HashMap`?**

Sector order in the report matches the order sectors appear in the query results. This makes it easy to verify output against the raw database data without mentally reordering rows.

**Why `GROUP BY period ORDER BY MIN(month_order)` in the periods query?**

PostgreSQL's `DISTINCT` requires any column in `ORDER BY` to also appear in the `SELECT` list. Since we only want period names in the output but need `month_order` purely for sorting, grouping by period and ordering by the minimum `month_order` within each group achieves calendar ordering cleanly without exposing the sort key in the result set.

---

## What the Data Represents

The portfolio holds five securities across four sectors — Technology (AAPL, MSFT), Financials (JPM), Energy (XOM), and Healthcare (PFE). Weights and returns shift month to month, simulating realistic portfolio drift and rebalancing decisions over a full year.

The benchmark is a five-sector index that also includes a Consumer sector the portfolio has no direct exposure to. This means the portfolio always shows a negative allocation effect for Consumer — it holds 0% against the benchmark's 10% weight. That's a deliberate design choice to make the attribution output more realistic and analytically interesting.

---


## Full-Year Results & Interpretation

### The headline

The portfolio outperformed the benchmark in every single month of the year. Total portfolio return came in at **66.14%** against a benchmark return of **52.30%**, generating **13.84% of cumulative active return** across 12 months. The average monthly outperformance was **1.15%**, which is a consistent and meaningful edge — not a one-month spike that flatters the annual number.

---

### How performance evolved through the year

The portfolio started conservatively and built momentum as the year progressed.

The first half of the year — January through June — produced **6.56% of cumulative active return**. January and June were the quietest months, both coming in at +0.65%. February was the first breakout month at +1.45%, driven by a strong Technology allocation call and surprisingly good security selection in Financials. May was the standout of the first half at **+1.95%** — the best single month of the entire year — where Technology allocation (+0.90%) and Healthcare selection (+0.50%) both fired at the same time.

The second half — July through December — was slightly stronger at **7.28% of cumulative active return**. October was the second-best month of the year at +1.65%, with Technology delivering its best selection effect of the year (+0.60%) on top of a solid allocation contribution. The portfolio closed the year well, with November and December both printing above +1.10%.

The only soft patch was the Q3 dip — June, August, and September all came in below +1.00%. Returns were still positive, but the portfolio's edge compressed. This lines up with periods where Technology's allocation contribution dropped to +0.50% and Healthcare selection went quiet.

---

### What actually drove the outperformance

**Technology was the engine.** Every single month, the overweight to Technology relative to the benchmark contributed positively to allocation effect, ranging from +0.50% in quieter months to +0.90% in May. On average, the Technology allocation call alone added **+0.71% per month**. That's not a coincidence — it reflects a deliberate and consistent decision to hold more Technology exposure than the benchmark, and it paid off reliably across all market conditions this year.

Technology selection was more variable. Some months it added meaningfully — October's +0.60% and July's +0.46% were the strongest — while other months it was flat. This suggests the sector-level overweight was the more reliable source of alpha than individual stock picking within Technology.

**Healthcare was the quiet outperformer.** Selection effect in Healthcare was positive in 10 out of 12 months, averaging **+0.25% per month**. It never had a blowout month, but it never hurt either. PFE consistently outperformed the benchmark's Healthcare return, making this the most dependable source of selection alpha in the portfolio.

**Financials was the complicated one.** The allocation effect was negative in months where JPM was underweighted relative to the benchmark's 20% Financials weight — costing small amounts in January, March, September, and November. But selection effect in Financials was positive whenever it showed up, particularly in February (+0.40%), May (+0.20%), July (+0.18%), October (+0.20%), November (+0.18%), and December (+0.20%). The story here is that the portfolio didn't always have the right *amount* of Financials exposure, but when it did hold Financials, JPM outperformed the sector benchmark. Selection saved what allocation occasionally gave back.

**Energy was a consistent drag on allocation.** The portfolio was underweight Energy relative to the benchmark in most months, which meant a negative allocation effect almost every period. Selection in Energy was occasionally positive — March (+0.30%), April (+0.12%), July (+0.12%) — but not enough to overcome the structural underweight. Energy was the one sector where the portfolio's positioning consistently worked against it, though the impact was small enough month to month that it never derailed overall performance.

**Consumer was a known structural negative.** The portfolio holds no Consumer exposure. The benchmark holds 10%. This means every month shows a small negative allocation effect for Consumer — and every month it was exactly that: small and predictable. It's the cost of not playing in that sector, and the outperformance elsewhere more than compensated for it year-round.

---

### The takeaway

This portfolio had a clear and repeatable thesis: overweight Technology, trust Healthcare stock selection, manage Financials carefully, and accept the drag from not holding Consumer or much Energy. That thesis was right more months than it was wrong, and the cumulative effect was 13.84% of active return generated above the benchmark over a full year. The consistency is arguably more impressive than the magnitude — there was no month where the portfolio underperformed, which means the edge wasn't luck concentrated in one or two months, but a structural positioning advantage that held across varying market conditions.

---

*Java 17 · PostgreSQL · JDBC · Maven · VS Code*