package ui;

import exception.*;
import model.BonusInfo;
import model.Medicine;
import model.PrescriptionType;
import model.Sale;
import service.BonusInterface;
import service.PharmacyServiceInterface;
import service.ReportServiceInterface;
import service.strategy.*;

import java.time.LocalDate; //дата без времени 2026-03-01
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private PharmacyServiceInterface service;
    private ReportServiceInterface<Sale> salesReportService;
    private ReportServiceInterface<Medicine> expiredReportService;
    private Scanner scanner = new Scanner(System.in);
    private BonusInterface bonus;

    public ConsoleUI(PharmacyServiceInterface service,
                     ReportServiceInterface<Sale> salesReportService,
                     ReportServiceInterface<Medicine> expiredReportService,
                     BonusInterface bonus) {
        this.service = service;
        this.salesReportService = salesReportService;
        this.expiredReportService = expiredReportService;
        this.bonus = bonus;
    }

    public void start() {
        while (true) {
            System.out.println("\n  АПТЕКА");
            System.out.println("1. Просмотреть список лекарств");
            System.out.println("2. Добавить лекарство");
            System.out.println("3. Удалить лекарство");
            System.out.println("4. Продать лекарство");
            System.out.println("5. Просмотреть журнал продаж");
            System.out.println("6. Отчёт по продажам ");
            System.out.println("7. Отчёт по просрочке");
            System.out.println("8. Бонусная система");
            System.out.println("0. Выход");

            System.out.print("Введите число: ");
            String input = scanner.nextLine();
            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1 -> viewMedicines();
                    case 2 -> addMedicine();
                    case 3 -> deleteMedicine();
                    //case 4 -> sellMedicine();
                    case 4 -> sellMedicineWithBonuses();
                    case 5 -> viewSales();
                    case 6 -> printSalesReport();
                    case 7 -> printExpiredReport();
                    case 8 -> bonusMenu();
                    case 0 -> { System.out.println("До свидания)"); return; }
                    default -> System.out.println("Неверный пункт меню(");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите число");
            } catch (PharmacyException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        }
    }

    private void viewMedicines() {
        System.out.println("\n  СПИСОК ЛЕКАРСТВ:");
        List<Medicine> medicines = service.getAllMedicines();
        if (medicines.isEmpty()) System.out.println("Список пуст");
        else medicines.forEach(System.out::println);
    }

    private void addMedicine() {
        System.out.println("\n  ДОБАВЛЕНИЕ ЛЕКАРСТВА");

        System.out.print("Название: ");
        String name = scanner.nextLine();

        PrescriptionType prescriptionType = readPrescriptionType("Требуется рецепт? (да/нет): ");
        int quantity = readInt("Количество: ");
        double price = readDouble("Цена (в рублях): ");
        LocalDate expirationDate = readDate("Годен до (дд.мм.гггг): ");

        service.addMedicine(new Medicine(name, prescriptionType, expirationDate, quantity, price));
    }

    private void deleteMedicine() {
        viewMedicines();
        System.out.print("Введите ID лекарства для удаления: ");
        String id = scanner.nextLine();
        service.deleteMedicine(id);
    }

    /*private void sellMedicine() {
        viewMedicines();
        System.out.print("Введите ID лекарства: ");
        String id = scanner.nextLine();
        int quantity = readInt("Введите количество: ");
        boolean hasPrescription = readYesNo("Есть рецепт? (да/нет): ");
        PricingStrategy strategy = chooseStrategy();
        service.sellMedicine(id, quantity, hasPrescription, strategy);
    }*/

    private void sellMedicineWithBonuses() {
        viewMedicines();
        System.out.print("Введите ID лекарства: ");
        String id = scanner.nextLine();
        int quantity = readInt("Введите количество: ");
        boolean hasPrescription = readYesNo("Есть рецепт? (да/нет): ");

        String clientPhone = readPhoneNumber("Введите номер телефона клиента (11 цифр, начинается с 8) или оставьте пустым: ");

        PricingStrategy strategy = chooseStrategy();

        service.sellMedicine(id, quantity, hasPrescription, strategy);

        List<Sale> sales = service.getSales();
        Sale lastSale = sales.get(sales.size() - 1);
        double saleAmount = lastSale.getTotalPrice();
        BonusInfo bonusInfo = bonus.getBonusInfo(clientPhone, saleAmount);

        int requestedBonus = 0;

        if (bonusInfo.canSpend()) {
            System.out.printf("У клиента %d бонусов. Максимум можно списать: %d (сумма покупки %d руб.)%n",
                    bonusInfo.getAvailableBonuses(),
                    bonusInfo.getMaxPossible(),
                    (int) saleAmount);
            requestedBonus = readInt("Сколько бонусов списать? (0 - не списывать): ");
        } else {
            System.out.println("Недостаточно бонусов для списания");
        }

        bonus.processBonusesForSale(clientPhone, saleAmount, lastSale, requestedBonus);
    }

    private void bonusMenu() {
        while (true) {
            System.out.println("\n  БОНУСНАЯ СИСТЕМА");
            System.out.println("1. Показать бонусы клиента по номеру телефона");
            System.out.println("2. Показать бонусный отчёт");
            System.out.println("3. Сбросить все данные");
            System.out.println("0. Назад");

            System.out.print("Выберите действие: ");
            String input = scanner.nextLine();
            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1 -> {
                        String phone = readPhoneNumber("Введите номер телефона (11 цифр, начинается с 8) или оставьте пустым: ");
                        if (!phone.isEmpty()) {
                            bonus.showBonuses(phone);
                        }
                    }
                    case 2 -> bonus.printBonusReport();
                    case 3 -> {
                        if (readYesNo("Подтвердите сброс всех бонусных данных (да/нет): ")) {
                            bonus.resetAllData();
                        }
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Неверный пункт");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите число");
            }
        }
    }

    private PricingStrategy chooseStrategy() {
        while (true) {
            System.out.println("Выбор скидки: 1 - Пенсионная (25%), 2 - Для многодетных семей (10%), 3 - Без скидки ");
            int choice = readInt("Ваш выбор: ");
            switch (choice) {
                case 1:
                    return new PensionerDiscountStrategy();
                case 2:
                    return new LargeFamilyDiscountStrategy();
                case 3:
                    return new NoDiscountStrategy();
                default:
                    System.out.println("Неверный выбор, введите 1,2 или 3");
            }
        }
    }

    private void viewSales() {
        System.out.println("\n  ЖУРНАЛ ПРОДАЖ:");
        List<Sale> sales = service.getSales();
        if (sales.isEmpty()) System.out.println("Список пуст");
        else sales.forEach(System.out::println);
    }

    private void printSalesReport() {
        List<Sale> sales = service.getSales();
        salesReportService.printReport(sales);
    }

    private void printExpiredReport() {
        List<Medicine> medicines = service.getAllMedicines();
        expiredReportService.printReport(medicines);
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("да")) return true;
            if (input.equals("нет")) return false;
            System.out.println("Введите 'да' или 'нет'");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Введите число!");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine());
                if (value <= 0) {
                    System.out.println("Цена должна быть положительной!");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Введите число (например: 150.50)!");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine(), formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Введите дату в формате дд.мм.гггг");
            }
        }
    }

    private String readPhoneNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) {
                return "";
            }
            if (!phone.startsWith("8") && phone.length() != 11) {
                System.out.println("Номер телефона должен начинаться с 8  содержать 11 цифр!");
                continue;
            }
            boolean onlyDigits = true;
            for (int i = 0; i < phone.length(); i++) {
                if (!Character.isDigit(phone.charAt(i))) {
                    onlyDigits = false;
                    break;
                }
            }
            if (!onlyDigits) {
                System.out.println("Номер телефона может содержать только цифры!");
                continue;
            }
            return phone;
        }
    }

    private PrescriptionType readPrescriptionType(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("да")) {
                return PrescriptionType.PRESCRIPTION;
            } else if (input.equals("нет")) {
                return PrescriptionType.WITHOUTPRESCRIPTION;
            }
            System.out.println("Введите 'да' или 'нет'");
        }
    }
}