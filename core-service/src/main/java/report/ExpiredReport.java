package report;

import model.Medicine;
import java.util.List;
import java.util.stream.Collectors;

public class ExpiredReport implements Report {
    private List<Medicine> medicines;

    public ExpiredReport(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n   ПРОСРОЧЕННЫЕ ЛЕКАРСТВА\n");

        List<Medicine> expired = medicines.stream()
                .filter(Medicine::isExpired)
                .collect(Collectors.toList());

        if (expired.isEmpty()) {
            sb.append("Нет просроченных лекарств\n");
        } else {
            sb.append("\n");
            for (Medicine med : expired) {
                sb.append(String.format("%s (ID: %s)\n", med.getName(), med.getId()));
            }
            sb.append(String.format("\nВСЕГО ПРОСРОЧЕНО: %d шт.\n", expired.size()));
        }

        return sb.toString();
    }
}