import client.ReferenceClient;
import model.Medicine;
import model.Sale;
import observer.*;
import report.ConsoleReportFactory;
import report.ReportFactory;
import repository.DataInitializer;
import repository.MedicineRepository;
import repository.Repository;
import repository.SaleRepository;
import service.*;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        Repository<Medicine, String> medicineRepo = new MedicineRepository();
        Repository<Sale, String> saleRepo = new SaleRepository();

        DataInitializer.initMedicineRepository(medicineRepo);

        ReferenceClient referenceClient = new ReferenceClient("localhost", 50051);

        ReportFactory reportFactory = new ConsoleReportFactory();

        PharmacyServiceInterface service = new PharmacyService(medicineRepo, saleRepo, referenceClient);

        ReportServiceInterface<Sale> salesReportService = new SalesReportService(reportFactory);
        ReportServiceInterface<Medicine> expiredReportService = new ExpiredReportService(reportFactory);
        BonusInterface bonusGod = new Bonus();

        service.addObserver(new AddedObserver());
        service.addObserver(new RemovedObserver());
        service.addObserver(new ExpiredObserver());
        service.addObserver(new SoldObserver());

        ConsoleUI ui = new ConsoleUI(service, salesReportService, expiredReportService, bonusGod);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                referenceClient.shutdown();
                System.out.println("gRPC клиент остановлен");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        ui.start();
    }
}