package org.example;

import static org.example.CapabilitySelfTest.*;

final class ReportBudgetFixture {
    public static void main(String[] args){run();}
    static void run(){
        long second=1_000_000_000L;var budget=new ReportBudget(600*second);
        check(budget.analysisNanos(0)==598*second,"Initial export margin lost");
        budget.observe(4*second);
        check(budget.reserveNanos()==14*second,"Measured worker/supervisor exports not reserved");
        check(budget.analysisNanos(580*second)==6*second,"Final epoch not bounded by export reserve");
        check(budget.analysisNanos(586*second)==0,"Started work inside export window");
        budget.observe(second);check(budget.reserveNanos()==14*second,"Smaller checkpoint erased measured worst cost");
        check(budget.analysisNanos(601*second)==0,"Expired deadline reopened");
        budget.observe(Long.MAX_VALUE);check(budget.analysisNanos(0)==0,"Overflow enlarged time budget");
        System.out.println("ReportBudgetFixture PASS: measured final export reserve bounds remaining analysis time.");
    }
}
