package observer;

import model.Medicine;

public class ExpiredObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType) {
        if (eventType == EventType.EXPIRED) {
            System.out.println("ВНИМАНИЕ! Просрочено: " + medicine.getName() + " (ID: " + medicine.getId() + ")");
        }
    }
}