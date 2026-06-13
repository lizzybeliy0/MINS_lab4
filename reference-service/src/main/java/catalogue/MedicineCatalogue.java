package catalogue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MedicineCatalogue {

    private static MedicineCatalogue instance;
    private final Map<String, MedicineInfo> catalogue = new ConcurrentHashMap<>();

    private MedicineCatalogue() {
        catalogue.put("1", new MedicineInfo("Парацетамол", false));
        catalogue.put("2", new MedicineInfo("Амоксициллин", true));
        catalogue.put("3", new MedicineInfo("Витаминки", false));
        catalogue.put("4", new MedicineInfo("Лекарство", false));
        catalogue.put("5", new MedicineInfo("Лечилка", false));
    }

    public static synchronized MedicineCatalogue getInstance() {
        if (instance == null) {
            instance = new MedicineCatalogue();
        }
        return instance;
    }

    public boolean exists(String id) {
        return catalogue.containsKey(id);
    }

    public boolean isPrescriptionRequired(String id) {
        MedicineInfo info = catalogue.get(id);
        return info != null && info.requiresPrescription;
    }

    public String getMedicineName(String id) {
        MedicineInfo info = catalogue.get(id);
        return info != null ? info.name : null;
    }

    public void addMedicine(String id, String name, boolean requiresPrescription) {
        catalogue.put(id, new MedicineInfo(name, requiresPrescription));
    }

    public boolean removeMedicine(String id) {
        return catalogue.remove(id) != null;
    }

    private static class MedicineInfo {
        String name;
        boolean requiresPrescription;

        MedicineInfo(String name, boolean requiresPrescription) {
            this.name = name;
            this.requiresPrescription = requiresPrescription;
        }
    }
}