interface PricingStrategy {
    int calculate(int basePrice, long days);
}

class RegularPricing implements PricingStrategy {
    public int calculate(int base, long days) {
        return base * (int) days;
    }
}

public class PriceCalculator {
    private PricingStrategy strategy;

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public int getTotal(int base, long days) {
        return strategy.calculate(base, days);
    }
}