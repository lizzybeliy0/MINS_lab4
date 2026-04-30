package model;

public enum PrescriptionType {
    WITHOUTPRESCRIPTION("Без рецепта"),
    PRESCRIPTION("Рецептурный");

    private final String displayName;

    PrescriptionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPrescriptionRequired() {
        return this == PRESCRIPTION;
    }
}