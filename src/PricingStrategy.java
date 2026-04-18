package strategy;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PATTERN 9 — STRATEGY  (Behavioral)
 * ─────────────────────────────────────────────────────────────────────────────
 * Intent
 *   Define a family of algorithms (pricing rules), encapsulate each one, and
 *   make them interchangeable.  Strategy lets the pricing algorithm vary
 *   independently from the clients that use it (ReservationBuilder, ReservationForm).
 *
 * Why it matters here
 *   The original recalculate() in ReservationForm hard-coded:
 *       total = nights * room.getPricePerNight()
 *   That works for standard bookings, but the hotel needs seasonal pricing,
 *   last-minute discounts, and holiday surcharges.  Without Strategy, every new
 *   rule requires editing the form.  With Strategy, you drop in a new class and
 *   select it from a JComboBox — zero changes elsewhere.
 *
 * Strategies provided
 *   StandardPricingStrategy  — base price × nights (no change)
 *   HolidayPricingStrategy   — 30 % surcharge (peak season / public holidays)
 *   OffSeasonPricingStrategy — 20 % discount  (low season)
 *   LastMinutePricingStrategy— 15 % discount  (booking within 3 days of check-in)
 *   WeekendPricingStrategy   — 10 % surcharge on each weekend night in the range
 *
 * How to wire it in (ReservationForm.java)
 *   1. Add a JComboBox<PricingStrategy>:
 *          PricingStrategy[] strategies = {
 *              new StandardPricingStrategy(),
 *              new HolidayPricingStrategy(),
 *              new OffSeasonPricingStrategy(),
 *              new LastMinutePricingStrategy(),
 *              new WeekendPricingStrategy()
 *          };
 *          cmbStrategy = UITheme.makeComboBox(strategies);
 *
 *   2. In recalculate(), replace the hard-coded formula:
 *          PricingStrategy strategy = (PricingStrategy) cmbStrategy.getSelectedItem();
 *          double total = strategy.calculateTotal(r.getPricePerNight(), (int) nights);
 *          spnTotal.setValue(total);
 *          spnDeposit.setValue(total * 0.20);
 *
 *   3. In ReservationBuilder chain:
 *          .applyPricing(room.getPricePerNight(), strategy)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface PricingStrategy {

    /**
     * Calculate the total reservation cost.
     *
     * @param basePricePerNight The room's stored pricePerNight value (pre-discount base).
     * @param numberOfNights    Number of nights derived from check-in / check-out dates.
     * @return                  The final total amount to charge the guest.
     */
    double calculateTotal(double basePricePerNight, int numberOfNights);

    /**
     * Human-readable label shown in the JComboBox renderer and in log output.
     */
    String getStrategyName();

    /** Default toString() so JComboBox displays the strategy name automatically. */
    default String toString_() { return getStrategyName(); }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Strategy 1 — Standard (no adjustment)
    // ══════════════════════════════════════════════════════════════════════════

    class StandardPricingStrategy implements PricingStrategy {

        @Override
        public double calculateTotal(double basePricePerNight, int numberOfNights) {
            double total = basePricePerNight * numberOfNights;
            System.out.printf("[Strategy] Standard: %.2f × %d nights = $%.2f%n",
                basePricePerNight, numberOfNights, total);
            return total;
        }

        @Override public String getStrategyName() { return "Standard Rate"; }
        @Override public String toString()        { return getStrategyName(); }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Strategy 2 — Holiday (+30 % surcharge)
    // ══════════════════════════════════════════════════════════════════════════

    class HolidayPricingStrategy implements PricingStrategy {
        private static final double SURCHARGE = 0.30;

        @Override
        public double calculateTotal(double basePricePerNight, int numberOfNights) {
            double base  = basePricePerNight * numberOfNights;
            double total = base * (1 + SURCHARGE);
            System.out.printf("[Strategy] Holiday (+30%%): base=$%.2f → total=$%.2f%n",
                base, total);
            return total;
        }

        @Override public String getStrategyName() { return "Holiday Rate (+30%)"; }
        @Override public String toString()        { return getStrategyName(); }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Strategy 3 — Off-season (−20 % discount)
    // ══════════════════════════════════════════════════════════════════════════

    class OffSeasonPricingStrategy implements PricingStrategy {
        private static final double DISCOUNT = 0.20;

        @Override
        public double calculateTotal(double basePricePerNight, int numberOfNights) {
            double base  = basePricePerNight * numberOfNights;
            double total = base * (1 - DISCOUNT);
            System.out.printf("[Strategy] Off-Season (-20%%): base=$%.2f → total=$%.2f%n",
                base, total);
            return total;
        }

        @Override public String getStrategyName() { return "Off-Season Rate (-20%)"; }
        @Override public String toString()        { return getStrategyName(); }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Strategy 4 — Last-Minute (−15 % when booked within 3 days)
    // ══════════════════════════════════════════════════════════════════════════

    class LastMinutePricingStrategy implements PricingStrategy {
        private static final double DISCOUNT       = 0.15;
        private static final long   THRESHOLD_DAYS = 3;

        @Override
        public double calculateTotal(double basePricePerNight, int numberOfNights) {
            double base = basePricePerNight * numberOfNights;

            // Check: is today within THRESHOLD_DAYS of check-in?
            // For the Strategy demo, we always apply the discount here;
            // a real implementation would accept the checkInDate as a parameter.
            double total = base * (1 - DISCOUNT);
            System.out.printf(
                "[Strategy] Last-Minute (-15%%, within %d days): base=$%.2f → total=$%.2f%n",
                THRESHOLD_DAYS, base, total);
            return total;
        }

        @Override public String getStrategyName() { return "Last-Minute Rate (-15%)"; }
        @Override public String toString()        { return getStrategyName(); }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Strategy 5 — Weekend Surcharge (+10 % per weekend night)
    // ══════════════════════════════════════════════════════════════════════════

    class WeekendPricingStrategy implements PricingStrategy {
        private static final double WEEKEND_SURCHARGE = 0.10;

        @Override
        public double calculateTotal(double basePricePerNight, int numberOfNights) {
            // Estimate: assume 2 out of every 7 nights are weekend nights
            int weekendNights  = Math.max(0, (int) Math.round(numberOfNights * 2.0 / 7.0));
            int weekdayNights  = numberOfNights - weekendNights;
            double total = (weekdayNights * basePricePerNight)
                         + (weekendNights * basePricePerNight * (1 + WEEKEND_SURCHARGE));
            System.out.printf(
                "[Strategy] Weekend (+10%% on %d weekend nights): total=$%.2f%n",
                weekendNights, total);
            return total;
        }

        @Override public String getStrategyName() { return "Weekend Rate (+10% Fri-Sat)"; }
        @Override public String toString()        { return getStrategyName(); }
    }
}
