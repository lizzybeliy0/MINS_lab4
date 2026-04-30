package service;

import exception.*;
import model.Medicine;
import model.Sale;
import observer.EventType;
import observer.Observer;
import repository.Repository;
import repository.SaleRepository;
import service.strategy.PricingStrategy;
import client.ReferenceClient;  // НОВЫЙ ИМПОРТ

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PharmacyService implements PharmacyServiceInterface {
    private Repository<Medicine, String> medicineRepo;
    private Repository<Sale, String> saleRepo;
    private List<Observer> observers = new ArrayList<>();
    private ReferenceClient referenceClient;  // НОВОЕ
    private String traceId;                   // НОВОЕ (можно генерировать при каждом вызове)

    // ИЗМЕНЁННЫЙ КОНСТРУКТОР
    public PharmacyService(Repository<Medicine, String> medicineRepo,
                           Repository<Sale, String> saleRepo,
                           ReferenceClient referenceClient) {
        this.medicineRepo = medicineRepo;
        this.saleRepo = saleRepo;
        this.referenceClient = referenceClient;
    }

    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ TRACE ID
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // ИЗМЕНЁННЫЙ addMedicine
    public void addMedicine(Medicine medicine) {
        if (medicine.isExpired()) {
            notifyObservers(medicine, EventType.EXPIRED);
            throw new ExpiredMedicineException("Препарат просрочен");
        }

        // НОВОЕ: синхронизация с Reference Service
        String traceId = generateTraceId();
        try {
            referenceClient.addMedicineToCatalogue(
                    medicine.getMedicineId(),
                    medicine.getName(),
                    medicine.isPrescriptionRequired(),
                    traceId
            );
            System.out.println("[TraceID: " + traceId + "] Синхронизировано со справочником");
        } catch (Exception e) {
            System.err.println("[TraceID: " + traceId + "] Не удалось синхронизировать: " + e.getMessage());
        }

        medicineRepo.add(medicine);
        notifyObservers(medicine, EventType.ADDED);
    }

    // ИЗМЕНЁННЫЙ deleteMedicine
    public void deleteMedicine(String id) {
        Medicine medicine = medicineRepo.findById(id);
        if (medicine == null) throw new MedicineNotFoundException("Лекарство не найдено");

        // НОВОЕ: удаление из справочника
        String traceId = generateTraceId();
        try {
            referenceClient.removeMedicineFromCatalogue(id, traceId);
        } catch (Exception e) {
            System.err.println("[TraceID: " + traceId + "] Не удалось удалить из справочника: " + e.getMessage());
        }

        medicineRepo.deleteById(id);
        notifyObservers(medicine, EventType.REMOVED);
    }

    // ИЗМЕНЁННЫЙ sellMedicine
    public void sellMedicine(String id, int quantity, boolean hasPrescription, PricingStrategy strategy) {
        String traceId = generateTraceId();

        // НОВОЕ: проверка через Reference Service
        boolean exists = referenceClient.checkMedicineExists(id, traceId);
        if (!exists) {
            throw new MedicineNotFoundException("Лекарство не найдено в справочнике (ID: " + id + ")");
        }

        boolean requiresPrescription = referenceClient.isPrescriptionRequired(id, traceId);
        if (requiresPrescription && !hasPrescription) {
            throw new PrescriptionRequiredException("Нужен рецепт для этого лекарства");
        }

        // СТАРАЯ ЛОГИКА (без изменений)
        Medicine med = medicineRepo.findById(id);
        if (med == null) throw new MedicineNotFoundException("Лекарство не найдено");
        if (med.isExpired()) {
            notifyObservers(med, EventType.EXPIRED);
            throw new ExpiredMedicineException("Препарат просрочен");
        }

        med.reduceQuantity(quantity);
        double[] prices = strategy.calculatePrice(med, quantity);
        double unitPrice = prices[0];
        double totalPrice = prices[1];

        Sale sale = new Sale(med, quantity, unitPrice, totalPrice);
        saleRepo.add(sale);
        notifyObservers(med, EventType.SOLD);

        System.out.println("[TraceID: " + traceId + "] Продажа выполнена успешно");
    }

    // ОСТАЛЬНЫЕ МЕТОДЫ БЕЗ ИЗМЕНЕНИЙ
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    private void notifyObservers(Medicine medicine, EventType eventType) {
        for (Observer observer : observers) {
            observer.update(medicine, eventType);
        }
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepo.findAll();
    }

    public List<Sale> getSales() {
        return saleRepo.findAll();
    }
}