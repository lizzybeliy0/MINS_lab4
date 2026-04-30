package observer;

import model.Medicine;

public class AddedObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType) {
        if (eventType == EventType.ADDED) {
            System.out.println("ДОБАВЛЕНО: " + medicine.getName() + " (ID: " + medicine.getId() + ")");
        }
    }
}