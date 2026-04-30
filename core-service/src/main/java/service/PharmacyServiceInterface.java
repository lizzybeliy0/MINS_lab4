package service;

import model.Medicine;
import model.Sale;
import observer.Observer;
import service.strategy.PricingStrategy;

import java.util.List;

public interface PharmacyServiceInterface {
    void addMedicine(Medicine medicine);
    void deleteMedicine(String id);
    void sellMedicine(String id, int quantity, boolean hasPrescription, PricingStrategy strategy);
    List<Medicine> getAllMedicines();
    List<Sale> getSales();
    void addObserver(Observer observer);

}