package service;

import model.Sale;
import report.Report;
import report.ReportFactory;

import java.util.List;

public class SalesReportService implements ReportServiceInterface<Sale> {
    private ReportFactory reportFactory;

    public SalesReportService(ReportFactory reportFactory) {
        this.reportFactory = reportFactory;
    }

    @Override
    public void printReport(List<Sale> sales) {
        Report report = reportFactory.createSalesReport(sales);
        System.out.println(report.generate());
    }
}