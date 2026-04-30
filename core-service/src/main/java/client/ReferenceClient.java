package client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pharmacy.proto.*;
import exception.ReferenceUnavailableException;

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

    public void addMedicineToCatalogue(String id, String name, boolean requiresPrescription, String traceId)
            throws ReferenceUnavailableException {
        try {
            AddMedicineRequest request = AddMedicineRequest.newBuilder()
                    .setId(id)
                    .setName(name)
                    .setRequiresPrescription(requiresPrescription)
                    .setTraceId(traceId)
                    .build();
            AddMedicineResponse response = stub.addMedicineToCatalogue(request);
            available = true;

            if (response.getSuccess()) {
                logger.info("[TraceID: " + traceId + "] Добавлено в справочник: " + name + " (ID: " + id + ")");
            } else {
                logger.warning("[TraceID: " + traceId + "] Не удалось добавить в справочник: " + name);
            }
        } catch (Exception e) {
            logger.severe("[TraceID: " + traceId + "] Reference Service недоступен (addMedicineToCatalogue)");
            available = false;
            throw new ReferenceUnavailableException(
                    "Reference Service недоступен. Операция не выполнена. Пожалуйста, убедитесь, что Reference Service запущен.");
        }
    }

    public void removeMedicineFromCatalogue(String id, String traceId)
            throws ReferenceUnavailableException {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(id)
                    .setTraceId(traceId)
                    .build();
            RemoveResponse response = stub.removeMedicineFromCatalogue(request);
            available = true;

            if (response.getSuccess()) {
                logger.info("[TraceID: " + traceId + "] Удалено из справочника: " + id);
            } else {
                logger.warning("[TraceID: " + traceId + "] Не найдено в справочнике: " + id);
            }
        } catch (Exception e) {
            logger.severe("[TraceID: " + traceId + "] Reference Service недоступен.");
            available = false;
            throw new ReferenceUnavailableException(
                    "Reference Service недоступен. Операция не выполнена. Пожалуйста, убедитесь, что Reference Service запущен.");
        }
    }

    public boolean checkMedicineExists(String medicineId, String traceId)
            throws ReferenceUnavailableException {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(medicineId)
                    .setTraceId(traceId)
                    .build();
            ExistsResponse response = stub.checkMedicineExists(request);
            available = true;
            logger.info("[TraceID: " + traceId + "] checkExists(" + medicineId + ") = " + response.getExists());
            return response.getExists();
        } catch (Exception e) {
            logger.severe("[TraceID: " + traceId + "] Reference Service недоступен (checkMedicineExists)");
            available = false;
            throw new ReferenceUnavailableException(
                    "Reference Service недоступен. Невозможно проверить существование лекарства. Пожалуйста, убедитесь, что Reference Service запущен.");
        }
    }


    public boolean isPrescriptionRequired(String medicineId, String traceId)
            throws ReferenceUnavailableException {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(medicineId)
                    .setTraceId(traceId)
                    .build();
            PrescriptionResponse response = stub.isPrescriptionRequired(request);
            available = true;
            logger.info("[TraceID: " + traceId + "] isPrescriptionRequired(" + medicineId + ") = " + response.getRequiresPrescription());
            return response.getRequiresPrescription();
        } catch (Exception e) {
            logger.severe("[TraceID: " + traceId + "] Reference Service недоступен (isPrescriptionRequired)");
            available = false;
            throw new ReferenceUnavailableException(
                    "Reference Service недоступен. Невозможно проверить необходимость рецепта. Пожалуйста, убедитесь, что Reference Service запущен.");
        }
    }

    public String getMedicineName(String medicineId, String traceId)
            throws ReferenceUnavailableException {
        try {
            MedicineIdRequest request = MedicineIdRequest.newBuilder()
                    .setMedicineId(medicineId)
                    .setTraceId(traceId)
                    .build();
            MedicineInfoResponse response = stub.getMedicineInfo(request);
            available = true;
            logger.info("[TraceID: " + traceId + "] getMedicineInfo(" + medicineId + ") = " + response.getName());

            if (!response.getExists()) {
                logger.warning("[TraceID: " + traceId + "] Лекарство не найдено в справочнике: " + medicineId);
                throw new ReferenceUnavailableException("Лекарство с ID " + medicineId + " не найдено в справочнике.");
            }
            return response.getName();
        } catch (ReferenceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("[TraceID: " + traceId + "] Reference Service недоступен (getMedicineName)");
            available = false;
            throw new ReferenceUnavailableException(
                    "Reference Service недоступен. Невозможно получить имя лекарства. Пожалуйста, убедитесь, что Reference Service запущен.");
        }
    }

    public boolean isAvailable() {
        return available;
    }
}