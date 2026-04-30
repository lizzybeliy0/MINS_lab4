package repository;

import model.Medicine;

import java.util.*;

public class MedicineRepository implements Repository<Medicine, String> {

    private Map<String, Medicine> storage = new HashMap<>();

    @Override
    public void add(Medicine medicine) {
        storage.put(medicine.getId(), medicine);
    }

    @Override
    public Medicine findById(String id) {
        return storage.get(id);
    }

    @Override
    public List<Medicine> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }
}