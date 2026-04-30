package repository;

import model.Sale;

import java.util.*;

public class SaleRepository implements Repository<Sale, String> {

    private Map<String, Sale> storage = new HashMap<>();

    @Override
    public void add(Sale sale) {
        storage.put(sale.getId(), sale);
    }

    @Override
    public Sale findById(String id) {
        return storage.get(id);
    }

    @Override
    public List<Sale> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }
}