package observer;

import model.Medicine;

public class RemovedObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType, String medicineName) {
        if (eventType == EventType.REMOVED) {
            System.out.println("УДАЛЕНО: " + medicineName + " (ID: " + medicine.getMedicineId() + ", партия: " + medicine.getId() + ")");
        }
    }
}