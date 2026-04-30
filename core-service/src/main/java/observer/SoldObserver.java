package observer;

import model.Medicine;

public class SoldObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType, String medicineName) {
        if (eventType == EventType.SOLD) {
            System.out.println("ПРОДАНО: " + medicineName + " (ID: " + medicine.getMedicineId() + ", партия: " + medicine.getId() + ")");
        }
    }
}