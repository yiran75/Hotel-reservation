public interface PricingStrategy {
    int calculate(int price, int days);
}

class RegularPricing implements PricingStrategy {
    @Override
    public int calculate(int price, int days) {
        return price * days;
    }
}