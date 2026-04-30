package observer;

import model.Medicine;

public class RemovedObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType) {
        if (eventType == EventType.REMOVED) {
            System.out.println("УДАЛЕНО: " + medicine.getName() + " (ID: " + medicine.getId() + ")");
        }
    }
}