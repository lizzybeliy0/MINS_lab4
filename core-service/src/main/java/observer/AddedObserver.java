package observer;

import model.Medicine;

public class AddedObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType, String medicineName) {
        if (eventType == EventType.ADDED) {
            System.out.println("ДОБАВЛЕНО: " + medicineName + " (ID: " + medicine.getMedicineId() + ", партия: " + medicine.getId() + ")");
        }
    }
}