package observer;

import model.Medicine;

public interface Observer {
    void update(Medicine medicine, EventType eventType);
}