package repository;

import model.Medicine;
import java.time.LocalDate;

public class DataInitializer {

    public static void initMedicineRepository(Repository<Medicine, String> repo) {
        // medicineId теперь — это ID из справочника Reference
        repo.add(new Medicine("1", LocalDate.now().plusDays(30), 50, 150.0));   // Парацетамол
        repo.add(new Medicine("2", LocalDate.now().plusDays(20), 20, 350.0));   // Амоксициллин
        repo.add(new Medicine("3", LocalDate.now().minusDays(5), 10, 500.0));   // Витаминки
        repo.add(new Medicine("4", LocalDate.now().minusDays(10), 10, 500.0));  // Лекарство
        repo.add(new Medicine("5", LocalDate.now().minusDays(15), 10, 500.0));  // Лечилка
    }
}