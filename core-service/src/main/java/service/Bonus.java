package service;

import model.BonusInfo;
import model.Sale;

import java.util.*;

public class Bonus implements BonusInterface{

    private static Map<String, Integer> clientBonuses = new HashMap<>();
    private static int totalBonusPointsIssued = 0; //начислено
    private static int totalBonusPointsSpent = 0;  //списано

    private static final double BONUS_RATE = 0.01;
    private static final int MIN_BONUS_TO_SPEND = 10;
    private static final int GOLD_THRESHOLD = 1000;
    private static final int SILVER_THRESHOLD = 500;
    private static final double GOLD_BONUS_MULTIPLIER = 1.5;
    private static final double SILVER_BONUS_MULTIPLIER = 1.2;
    private static final int BONUS_TO_DISCOUNT_RATE = 1;

    public Bonus() {
        loadBonusesFromFile();
    }

    @Override
    public double processBonusesForSale(String clientPhone, double saleAmount, Sale sale, int requestedBonus) {

        System.out.printf("Сумма покупки: %.2f руб.%n", saleAmount);

        int availableBonuses = getAvailableBonuses(clientPhone);
        int discountFromBonuses = 0;
        int maxPossible = Math.min(availableBonuses, (int) saleAmount);
        int actualToSpend = 0;

        if (requestedBonus > 0) {
            if (requestedBonus > maxPossible) {
                System.out.printf("Вы запросили %d бонусов, но максимум: %d. Будет списано %d%n",
                        requestedBonus, maxPossible, maxPossible);
                actualToSpend = maxPossible;
            }
            else if (requestedBonus < MIN_BONUS_TO_SPEND) {
                System.out.printf("Минимальное списание: %d баллов%n", MIN_BONUS_TO_SPEND);
                actualToSpend = 0;
            } else {
                actualToSpend = requestedBonus;
            }
        }
        if (actualToSpend >= MIN_BONUS_TO_SPEND) {
            int spent = spendBonuses(clientPhone, actualToSpend);
            discountFromBonuses = spent * BONUS_TO_DISCOUNT_RATE;
            System.out.printf("Списано %d бонусов, скидка %d руб.%n", spent, discountFromBonuses);
        } else {
            System.out.println("Бонусы не списывались");
        }

        double finalAmount = saleAmount - discountFromBonuses;
        if (finalAmount < 0) finalAmount = 0;

        System.out.printf("Итого к оплате: %.2f руб.%n", finalAmount);

        int newBonuses = addBonusForSale(clientPhone, finalAmount, sale);
        if (newBonuses > 0) {
            System.out.printf("Начислено %d бонусов за покупку!%n", newBonuses);
        }

        return finalAmount;
    }

    @Override
    public BonusInfo getBonusInfo(String clientPhone, double saleAmount) {
        int availableBonuses = getAvailableBonuses(clientPhone);
        int maxPossible = Math.min(availableBonuses, (int) saleAmount);
        return new BonusInfo(availableBonuses, maxPossible, maxPossible >= MIN_BONUS_TO_SPEND);
    }

    public int getAvailableBonuses(String clientPhone) {
        if (clientPhone == null) return 0;
        return clientBonuses.getOrDefault(clientPhone, 0);
    }

    private int spendBonuses(String clientPhone, int bonusesToSpend) {
        int current = clientBonuses.getOrDefault(clientPhone, 0);
        int actualToSpend = Math.min(bonusesToSpend, current);

        clientBonuses.put(clientPhone, current - actualToSpend);
        totalBonusPointsSpent += actualToSpend;

        saveBonusesToFile();
        return actualToSpend;
    }

    private int addBonusForSale(String clientPhone, double saleAmount, Sale sale) {
        if (clientPhone == null || clientPhone.trim().isEmpty()) {
            System.out.println("Результат без использования бонусной карты");
            return 0;
        }

        if (saleAmount <= 0) {
            return 0;
        }

        int bonusToAdd = calculateBonus(clientPhone, saleAmount);
        clientBonuses.put(clientPhone, clientBonuses.getOrDefault(clientPhone, 0) + bonusToAdd);
        totalBonusPointsIssued += bonusToAdd;

        checkClientLevel(clientPhone);
        saveBonusesToFile();
        return bonusToAdd;
    }

    private int calculateBonus(String clientPhone, double amount) {
        int baseBonus = (int)(amount * BONUS_RATE);
        int currentBonuses = clientBonuses.getOrDefault(clientPhone, 0);

        if (currentBonuses >= GOLD_THRESHOLD) {
            baseBonus = (int)(baseBonus * GOLD_BONUS_MULTIPLIER);
            System.out.println("Золотой клиент! Бонус ×1.5");
        } else if (currentBonuses >= SILVER_THRESHOLD) {
            baseBonus = (int)(baseBonus * SILVER_BONUS_MULTIPLIER);
            System.out.println("Серебряный клиент! Бонус ×1.2");
        }

        return Math.max(1, baseBonus);
    }

    @Override
    public void showBonuses(String clientPhone) {
        if (clientPhone == null || clientPhone.trim().isEmpty()) {
            System.out.println("Введите номер телефона клиента");
            return;
        }

        int bonuses = clientBonuses.getOrDefault(clientPhone, 0);
        int level = getClientLevel(clientPhone);
        String levelName = level == 2 ? "ЗОЛОТОЙ" : level == 1 ? "СЕРЕБРЯНЫЙ" : "ОБЫЧНЫЙ";

        System.out.println("\nКЛИЕНТ: " + maskPhone(clientPhone));
        System.out.println("УРОВЕНЬ: " + levelName);
        System.out.println("БОНУСОВ НА СЧЕТУ: " + bonuses);
        System.out.println("Можете получить скидку до " + (bonuses * BONUS_TO_DISCOUNT_RATE) + " руб.");
    }

    @Override
    public void printBonusReport() {
        System.out.println("\n  БОНУСНЫЙ ОТЧЁТ");
        System.out.println("СТАТИСТИКА за сегодня:");
        System.out.printf("   Всего начислено баллов: %d%n", totalBonusPointsIssued);
        System.out.printf("   Всего списано баллов:   %d%n", totalBonusPointsSpent);
        System.out.printf("   Активированных клиентов: %d%n", clientBonuses.size());

        if (!clientBonuses.isEmpty()) {
            System.out.println("\n  ТОП-3 ПО БАЛЛАМ:");
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(clientBonuses.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            for (int i = 0; i < Math.min(3, sorted.size()); i++) {
                System.out.printf("   %d. %s — %d баллов%n", i+1,
                        maskPhone(sorted.get(i).getKey()), sorted.get(i).getValue());
            }
        }
    }

    @Override
    public void resetAllData() {
        clientBonuses.clear();
        totalBonusPointsIssued = 0;
        totalBonusPointsSpent = 0;
        System.out.println("Все бонусные данные сброшены");
        saveBonusesToFile();
    }

    private void checkClientLevel(String clientPhone) {
        int bonuses = clientBonuses.getOrDefault(clientPhone, 0);
        if (bonuses >= GOLD_THRESHOLD && bonuses < GOLD_THRESHOLD + 100) {
            System.out.println("    ПОЗДРАВЛЯЕМ! Вы достигли ЗОЛОТОГО уровня!");
        } else if (bonuses >= SILVER_THRESHOLD && bonuses < SILVER_THRESHOLD + 50) {
            System.out.println("    Поздравляем! Вы достигли СЕРЕБРЯНОГО уровня!");
        }
    }

    private int getClientLevel(String clientPhone) {
        int bonuses = clientBonuses.getOrDefault(clientPhone, 0);
        if (bonuses >= GOLD_THRESHOLD) return 2;
        if (bonuses >= SILVER_THRESHOLD) return 1;
        return 0;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "***";

        int visibleCount = 4;
        int maskLength = phone.length() - visibleCount;

        String masked = "*".repeat(maskLength);
        String visible = phone.substring(phone.length() - visibleCount);

        return masked + visible;
    }

    private void loadBonusesFromFile() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("./bonus_data.txt");
            if (java.nio.file.Files.exists(path)) {
                List<String> lines = java.nio.file.Files.readAllLines(path);

                for (String line : lines) {
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        clientBonuses.put(parts[0], Integer.parseInt(parts[1]));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА: Не удалось загрузить бонусные данные: " + e.getMessage());
            throw new RuntimeException("Ошибка загрузки бонусных данных", e);
        }
    }

    private void saveBonusesToFile() {
        try (java.io.FileWriter writer = new java.io.FileWriter("./bonus_data.txt")) {
            for (Map.Entry<String, Integer> entry : clientBonuses.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения бонусных данных", e);
        }
    }
}