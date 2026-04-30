package service;

import model.Medicine;
import report.Report;
import report.ReportFactory;

import java.util.List;

public class ExpiredReportService implements ReportServiceInterface<Medicine> {
    private ReportFactory reportFactory;

    public ExpiredReportService(ReportFactory reportFactory) {
        this.reportFactory = reportFactory;
    }

    @Override
    public void printReport(List<Medicine> medicines) {
        Report report = reportFactory.createExpiredReport(medicines);
        System.out.println(report.generate());
    }
}