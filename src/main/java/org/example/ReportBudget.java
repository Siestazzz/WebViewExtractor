package org.example;

/** Reserves measured export time without enlarging the process deadline. */
final class ReportBudget {
    final long deadline;
    long maximumExportNanos;
    ReportBudget(long deadline){this.deadline=deadline;}
    void observe(long elapsed){maximumExportNanos=Math.max(maximumExportNanos,Math.max(0,elapsed));}
    long reserveNanos(){
        // Final worker build/export plus supervisor read/export and a margin.
        long base=2_000_000_000L;
        return maximumExportNanos>(Long.MAX_VALUE-base)/3?Long.MAX_VALUE:base+3*maximumExportNanos;
    }
    long analysisNanos(long now){
        long remaining=deadline-now,reserve=reserveNanos();
        return remaining<=reserve?0:remaining-reserve;
    }
}
