package service;

import io.grpc.stub.StreamObserver;
import pharmacy.proto.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ReferenceServiceImpl extends ReferenceServiceGrpc.ReferenceServiceImplBase {

    private static final Logger logger = Logger.getLogger(ReferenceServiceImpl.class.getName());
    private final Map<String, CatalogueEntry> catalogue = new ConcurrentHashMap<>();

    public ReferenceServiceImpl() {
        // Инициализация начальными данными (синхронизация с Core после старта)
        // Будет заполнено при добавлении лекарств из Core
    }

    @Override
    public void checkMedicineExists(MedicineIdRequest request,
                                    StreamObserver<ExistsResponse> responseObserver) {
        String traceId = request.getTraceId();
        String id = request.getMedicineId();

        boolean exists = catalogue.containsKey(id);
        logger.info(String.format("[TraceID: %s] checkExists(%s) = %s", traceId, id, exists));

        responseObserver.onNext(ExistsResponse.newBuilder()
                .setExists(exists)
                .setMessage(exists ? "Найдено" : "Не найдено")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void isPrescriptionRequired(MedicineIdRequest request,
                                       StreamObserver<PrescriptionResponse> responseObserver) {
        String traceId = request.getTraceId();
        String id = request.getMedicineId();

        CatalogueEntry entry = catalogue.get(id);
        boolean requires = entry != null && entry.requiresPrescription;

        logger.info(String.format("[TraceID: %s] isPrescriptionRequired(%s) = %s", traceId, id, requires));

        responseObserver.onNext(PrescriptionResponse.newBuilder()
                .setRequiresPrescription(requires)
                .setMedicineName(entry != null ? entry.name : "Неизвестно")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addMedicineToCatalogue(AddMedicineRequest request,
                                       StreamObserver<AddMedicineResponse> responseObserver) {
        String traceId = request.getTraceId();
        String id = request.getId();
        String name = request.getName();
        boolean requires = request.getRequiresPrescription();

        catalogue.put(id, new CatalogueEntry(name, requires));
        logger.info(String.format("[TraceID: %s] Добавлено в каталог: %s -> %s (рецепт: %s)",
                traceId, id, name, requires));

        responseObserver.onNext(AddMedicineResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Добавлено в справочник")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeMedicineFromCatalogue(MedicineIdRequest request,
                                            StreamObserver<RemoveResponse> responseObserver) {
        String traceId = request.getTraceId();
        String id = request.getMedicineId();

        boolean removed = catalogue.remove(id) != null;
        logger.info(String.format("[TraceID: %s] Удалено из каталога: %s (успешно: %s)", traceId, id, removed));

        responseObserver.onNext(RemoveResponse.newBuilder()
                .setSuccess(removed)
                .setMessage(removed ? "Удалено" : "Не найдено")
                .build());
        responseObserver.onCompleted();
    }

    private static class CatalogueEntry {
        String name;
        boolean requiresPrescription;

        CatalogueEntry(String name, boolean requiresPrescription) {
            this.name = name;
            this.requiresPrescription = requiresPrescription;
        }
    }
}