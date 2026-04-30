package service;

import exception.*;
import model.Medicine;
import model.Sale;
import observer.EventType;
import observer.Observer;
import repository.Repository;
import repository.SaleRepository;
import service.strategy.PricingStrategy;
import client.ReferenceClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PharmacyService implements PharmacyServiceInterface {
    private Repository<Medicine, String> medicineRepo;
    private Repository<Sale, String> saleRepo;
    private List<Observer> observers = new ArrayList<>();
    private ReferenceClient referenceClient;

    public PharmacyService(Repository<Medicine, String> medicineRepo,
                           Repository<Sale, String> saleRepo,
                           ReferenceClient referenceClient) {
        this.medicineRepo = medicineRepo;
        this.saleRepo = saleRepo;
        this.referenceClient = referenceClient;
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Получить имя из Reference
    public String getMedicineName(String medicineId, String traceId) {
        try {
            return referenceClient.getMedicineName(medicineId, traceId);
        } catch (Exception e) {
            System.err.println("[TraceID: " + traceId + "] Не удалось получить имя: " + e.getMessage());
            return "Неизвестно";
        }
    }

    // Уведомление с именем
    private void notifyObservers(Medicine medicine, EventType eventType, String medicineName) {
        for (Observer observer : observers) {
            observer.update(medicine, eventType, medicineName);
        }
    }

    // Уведомление без имени (когда имени ещё нет — например, при добавлении)
    private void notifyObservers(Medicine medicine, EventType eventType) {
        String traceId = generateTraceId();
        String medicineName = getMedicineName(medicine.getMedicineId(), traceId);
        notifyObservers(medicine, eventType, medicineName);
    }

    public void addMedicine(Medicine medicine, String name, boolean requiresPrescription) {
        String traceId = generateTraceId();

        if (medicine.isExpired()) {
            notifyObservers(medicine, EventType.EXPIRED);
            throw new ExpiredMedicineException("Препарат просрочен");
        }

        // Синхронизация с Reference
        try {
            referenceClient.addMedicineToCatalogue(
                    medicine.getMedicineId(),
                    name,
                    requiresPrescription,
                    traceId
            );
            System.out.println("[TraceID: " + traceId + "] Синхронизировано со справочником");
        } catch (Exception e) {
            System.err.println("[TraceID: " + traceId + "] Не удалось синхронизировать: " + e.getMessage());
        }

        medicineRepo.add(medicine);

        // Уведомление с именем (имя уже знаем)
        notifyObservers(medicine, EventType.ADDED, name);
    }

    public void deleteMedicine(String id) {
        String traceId = generateTraceId();

        Medicine medicine = medicineRepo.findById(id);
        if (medicine == null) throw new MedicineNotFoundException("Лекарство не найдено");

        // Получаем имя перед удалением
        String medicineName = getMedicineName(medicine.getMedicineId(), traceId);

        try {
            referenceClient.removeMedicineFromCatalogue(medicine.getMedicineId(), traceId);
        } catch (Exception e) {
            System.err.println("[TraceID: " + traceId + "] Не удалось удалить из справочника: " + e.getMessage());
        }

        medicineRepo.deleteById(id);

        // Уведомление с именем
        notifyObservers(medicine, EventType.REMOVED, medicineName);
    }

    public void sellMedicine(String medicineId, int quantity, boolean hasPrescription, PricingStrategy strategy) {
        String traceId = generateTraceId();

        // Проверка через Reference
        boolean exists = referenceClient.checkMedicineExists(medicineId, traceId);
        if (!exists) {
            throw new MedicineNotFoundException("Лекарство не найдено в справочнике (ID: " + medicineId + ")");
        }

        boolean requiresPrescription = referenceClient.isPrescriptionRequired(medicineId, traceId);
        if (requiresPrescription && !hasPrescription) {
            throw new PrescriptionRequiredException("Нужен рецепт");
        }

        // Получаем имя для уведомления и Sale
        String medicineName = getMedicineName(medicineId, traceId);

        // Ищем партию на складе
        List<Medicine> medicines = medicineRepo.findAll();
        Medicine med = medicines.stream()
                .filter(m -> m.getMedicineId().equals(medicineId))
                .findFirst()
                .orElseThrow(() -> new MedicineNotFoundException("Нет в наличии лекарства с ID: " + medicineId));

        if (med.isExpired()) {
            notifyObservers(med, EventType.EXPIRED, medicineName);
            throw new ExpiredMedicineException("Препарат просрочен");
        }

        med.reduceQuantity(quantity);
        double[] prices = strategy.calculatePrice(med, quantity);

        Sale sale = new Sale(medicineName, quantity, prices[0], prices[1]);
        saleRepo.add(sale);

        // Уведомление с именем
        notifyObservers(med, EventType.SOLD, medicineName);

        System.out.println("[TraceID: " + traceId + "] Продажа выполнена успешно");
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepo.findAll();
    }

    public List<Sale> getSales() {
        return saleRepo.findAll();
    }
}