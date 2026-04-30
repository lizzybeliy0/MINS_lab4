package model;

public class BonusInfo {
    private final int availableBonuses;
    private final int maxPossible;
    private final boolean canSpend;

    public BonusInfo(int availableBonuses, int maxPossible, boolean canSpend) {
        this.availableBonuses = availableBonuses;
        this.maxPossible = maxPossible;
        this.canSpend = canSpend;
    }

    public int getAvailableBonuses() { return availableBonuses; }
    public int getMaxPossible() { return maxPossible; }
    public boolean canSpend() { return canSpend; }
}