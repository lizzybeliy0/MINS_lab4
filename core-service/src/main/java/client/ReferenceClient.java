package client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pharmacy.proto.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ReferenceClient {
    private static final Logger logger = Logger.getLogger(ReferenceClient.class.getName());

    private final ManagedChannel channel;
    private final ReferenceServiceGrpc.ReferenceServiceBlockingStub stub;
    private boolean available = true;

    public ReferenceClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = ReferenceServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public boolean checkMedicineExists(String medicineId, String traceId) {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(medicineId)
                    .setTraceId(traceId)
                    .build();
            ExistsResponse response = stub.checkMedicineExists(request);
            return response.getExists();
        } catch (Exception e) {
            logger.warning("[TraceID: " + traceId + "] Reference Service недоступен: " + e.getMessage());
            available = false;
            return fallbackExists(medicineId);
        }
    }

    public boolean isPrescriptionRequired(String medicineId, String traceId) {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(medicineId)
                    .setTraceId(traceId)
                    .build();
            PrescriptionResponse response = stub.isPrescriptionRequired(request);
            return response.getRequiresPrescription();
        } catch (Exception e) {
            logger.warning("[TraceID: " + traceId + "] Reference Service недоступен: " + e.getMessage());
            available = false;
            return false;  // fallback: считаем что рецепт не нужен
        }
    }

    public boolean addMedicineToCatalogue(String id, String name, boolean requiresPrescription, String traceId) {
        try {
            AddMedicineRequest request = AddMedicineRequest.newBuilder()
                    .setId(id)
                    .setName(name)
                    .setRequiresPrescription(requiresPrescription)
                    .setTraceId(traceId)
                    .build();
            AddMedicineResponse response = stub.addMedicineToCatalogue(request);
            return response.getSuccess();
        } catch (Exception e) {
            logger.warning("[TraceID: " + traceId + "] Не удалось синхронизировать: " + e.getMessage());
            return false;
        }
    }

    public boolean removeMedicineFromCatalogue(String id, String traceId) {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(id)
                    .setTraceId(traceId)
                    .build();
            RemoveResponse response = stub.removeMedicineFromCatalogue(request);
            return response.getSuccess();
        } catch (Exception e) {
            logger.warning("[TraceID: " + traceId + "] Не удалось удалить из справочника: " + e.getMessage());
            return false;
        }
    }

    private boolean fallbackExists(String medicineId) {
        // Если Reference недоступен, проверяем по формату ID
        return medicineId != null && medicineId.matches("\\d+");
    }
}