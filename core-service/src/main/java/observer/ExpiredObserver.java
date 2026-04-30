package observer;

import model.Medicine;

public class ExpiredObserver implements Observer {
    @Override
    public void update(Medicine medicine, EventType eventType, String medicineName) {
        if (eventType == EventType.EXPIRED) {
            System.out.println("ВНИМАНИЕ! Лекарство " + medicineName + " просрочено" );
        }
    }
}