package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Sale {
    private static int counter = 1;
    private String id;
    private String medicineName;    // ← кэш имени на момент продажи
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private LocalDateTime date;

    public Sale(String medicineName, int quantity, double unitPrice, double totalPrice) {
        this.id = String.valueOf(counter++);
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.date = LocalDateTime.now();
    }

    public String getId() { return id; }
    public double getTotalPrice() { return totalPrice; }
    public String getMedicineName() { return medicineName; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        return String.format("ID: %s | %s | %s | %d x %.2f = %.2f руб.",
                id, date.format(formatter), medicineName, quantity, unitPrice, totalPrice);
    }
}