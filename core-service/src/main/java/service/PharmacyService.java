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
import java.util.logging.Logger;

public class PharmacyService implements PharmacyServiceInterface {

    private static final Logger logger = Logger.getLogger(PharmacyService.class.getName());

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

    @Override
    public String getMedicineName(String medicineId, String traceId) {
        String name = referenceClient.getMedicineName(medicineId, traceId);
        if (name == null && !referenceClient.isAvailable()) {
            return "[Справочник недоступен]";
        }
        return name != null ? name : "Неизвестно";
    }

    // с именем
    private void notifyObservers(Medicine medicine, EventType eventType, String medicineName) {
        for (Observer observer : observers) {
            observer.update(medicine, eventType, medicineName);
        }
    }

    //без имени
    private void notifyObservers(Medicine medicine, EventType eventType) {
        String traceId = generateTraceId();
        String medicineName = getMedicineName(medicine.getMedicineId(), traceId);
        notifyObservers(medicine, eventType, medicineName);
    }

    @Override
    public void addMedicine(Medicine medicine, String name, boolean requiresPrescription) {
        String traceId = generateTraceId();

        if (medicine.isExpired()) {
            notifyObservers(medicine, EventType.EXPIRED);
            throw new ExpiredMedicineException("Препарат просрочен");
        }

        referenceClient.addMedicineToCatalogue(
                medicine.getMedicineId(),
                name,
                requiresPrescription,
                traceId
        );

        medicineRepo.add(medicine);
        notifyObservers(medicine, EventType.ADDED, name);
    }

    @Override
    public void deleteMedicine(String id) {
        String traceId = generateTraceId();

        Medicine medicine = medicineRepo.findById(id);
        if (medicine == null) {
            logger.warning("[TraceID: " + traceId + "] Лекарство не найдено: " + id);
            throw new MedicineNotFoundException("Лекарство не найдено");
        }

        String medicineName = getMedicineName(medicine.getMedicineId(), traceId);

        referenceClient.removeMedicineFromCatalogue(medicine.getMedicineId(), traceId);
        medicineRepo.deleteById(id);

        notifyObservers(medicine, EventType.REMOVED, medicineName);
    }

    @Override
    public void sellMedicine(String medicineId, int quantity, boolean hasPrescription, PricingStrategy strategy) {
        String traceId = generateTraceId();

        boolean exists = referenceClient.checkMedicineExists(medicineId, traceId);
        if (!exists) {
            throw new MedicineNotFoundException("Лекарство не найдено в справочнике (ID: " + medicineId + ")");
        }

        boolean requiresPrescription = referenceClient.isPrescriptionRequired(medicineId, traceId);
        if (requiresPrescription && !hasPrescription) {
            throw new PrescriptionRequiredException("Нужен рецепт");
        }

        String medicineName = getMedicineName(medicineId, traceId);

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
        logger.info("[TraceID: " + traceId + "] Продано: " + medicineName);

        notifyObservers(med, EventType.SOLD, medicineName);
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public List<Medicine> getAllMedicines() {
        return medicineRepo.findAll();
    }

    @Override
    public List<Sale> getSales() {
        return saleRepo.findAll();
    }
}