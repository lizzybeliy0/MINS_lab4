package report;

import model.Medicine;
import model.Sale;
import java.util.List;

public abstract class ReportFactory {
    public abstract Report createSalesReport(List<Sale> sales);
    public abstract Report createExpiredReport(List<Medicine> medicines);
}