package report;

import model.Medicine;
import model.Sale;
import java.util.List;

public class ConsoleReportFactory extends ReportFactory {

    @Override
    public Report createSalesReport(List<Sale> sales) {
        return new SalesReport(sales);
    }

    @Override
    public Report createExpiredReport(List<Medicine> medicines) {
        return new ExpiredReport(medicines);
    }
}