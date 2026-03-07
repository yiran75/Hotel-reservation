public class PriceCalculator {
    private PricingStrategy strategy;

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public int calculateTotal(int base, int days) {
        if (strategy == null) {
            strategy = new RegularPricing();
        }
        return strategy.calculate(base, days);
    }
}