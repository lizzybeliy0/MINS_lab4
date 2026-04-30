package model;

import exception.InsufficientQuantityException;
import exception.InvalidInputException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Medicine {
    private static int counter = 1;
    private String id;
    private String medicineId;
    private LocalDate expirationDate;
    private int quantity;
    private double price;

    public Medicine(String medicineId, LocalDate expirationDate, int quantity, double price) {
        if (quantity <= 0) throw new InvalidInputException("Количество должно быть положительным");
        if (price <= 0) throw new InvalidInputException("Цена должна быть положительной");
        this.id = String.valueOf(counter++);
        this.medicineId = medicineId;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() { return id; }
    public String getMedicineId() { return medicineId; }
    public double getPrice() { return price; }
    public boolean isExpired() { return expirationDate.isBefore(LocalDate.now()); }
    public int getQuantity() { return quantity; }
    public LocalDate getExpirationDate() { return expirationDate; }

    public void reduceQuantity(int amount) {
        if (amount <= 0) throw new InvalidInputException("Количество должно быть больше 0");
        if (amount > quantity) throw new InsufficientQuantityException("Недостаточно товара");
        quantity -= amount;
    }

    @Override
    public String toString() {
        String expired = isExpired() ? " (просрочено)" : "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return String.format("ID: %s | Ссылка: %s | Цена: %.2f | Кол-во: %d | Годен до: %s%s",
                id, medicineId, price, quantity, expirationDate.format(formatter), expired);
    }
}