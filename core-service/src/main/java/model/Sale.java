package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Sale {
    private static int counter = 1;
    private String id;
    private Medicine medicine;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private LocalDateTime date;

    public Sale(Medicine medicine, int quantity, double unitPrice, double totalPrice) {
        this.id = String.valueOf(counter++);
        this.medicine = medicine;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.date = LocalDateTime.now();
    }

    public String getId() { return id; }
    public double getTotalPrice() { return totalPrice; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        if (unitPrice < medicine.getPrice()) {
            double discountPercent = (1 - unitPrice / medicine.getPrice()) * 100;
            return String.format("ID: %s | %s | %s | %d x %.2f = %.2f руб. (скидка %.0f%%)",
                    id, date.format(formatter), medicine.getName(),
                    quantity, unitPrice, totalPrice, discountPercent);
        } else {
            return String.format("ID: %s | %s | %s | %d x %.2f = %.2f руб.",
                    id, date.format(formatter), medicine.getName(),
                    quantity, unitPrice, totalPrice);
        }
    }
}