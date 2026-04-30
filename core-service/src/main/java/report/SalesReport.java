package report;

import model.Sale;
import java.util.List;

public class SalesReport implements Report {
    private List<Sale> sales;

    public SalesReport(List<Sale> sales) {
        this.sales = sales;
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n   ОТЧЁТ ПО ПРОДАЖАМ\n");

        if (sales.isEmpty()) {
            sb.append("Продаж не было\n");
        } else {
            for (Sale sale : sales) {
                sb.append(sale.toString()).append("\n");
            }
            double total = sales.stream()//поток
                    .mapToDouble(Sale::getTotalPrice)
                    .sum();
            sb.append(String.format("ВЫРУЧКА: %.2f руб.\n", total));
            sb.append(String.format("КОЛИЧЕСТВО ПРОДАЖ: %d\n", sales.size()));
        }

        return sb.toString();
    }
}