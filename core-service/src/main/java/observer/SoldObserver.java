package observer;

import model.Medicine;

public class SoldObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType) {
        if (eventType == EventType.SOLD) {
            System.out.println("ПРОДАНО: " + medicine.getName() + " (ID: " + medicine.getId() + ")");
        }
    }
}