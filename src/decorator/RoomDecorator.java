package decorator;

import enums.RoomStatus;
import enums.RoomType;
import model.Room;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PATTERN 5 — DECORATOR  (Structural)
 * ─────────────────────────────────────────────────────────────────────────────
 * Intent
 *   Attach additional responsibilities to an object dynamically.  Decorators
 *   provide a flexible alternative to subclassing for extending functionality.
 *
 * Why it matters here
 *   A room's base price covers accommodation only.  Guests can optionally add:
 *     • Spa access         (+$50/night)
 *     • Airport transfer   (+$75 flat)
 *     • Breakfast package  (+$30/night)
 *     • Late checkout      (+$40 flat)
 *   These add-ons are combinable: a Deluxe room with Spa AND Breakfast should
 *   cost base + $50/night + $30/night.  Modelling every combination with
 *   subclasses (DeluxeRoomWithSpa, DeluxeRoomWithSpaAndBreakfast …) is
 *   exponential.  Decorators compose at runtime:
 *       Room priced = new BreakfastDecorator(
 *                         new SpaAccessDecorator(deluxeRoom));
 *       double nightly = priced.getPricePerNight();  // base + 50 + 30
 *       String amenities = priced.getAmenities();    // "…, Spa, Breakfast"
 *
 * How to wire it in (ReservationForm / HotelBookingFacade)
 *   After RoomFactory creates the base room, apply any checked add-ons:
 *
 *       Room room = RoomFactory.create(type, id, number, price, cap, floor, desc);
 *       if (chkSpa.isSelected())       room = new SpaAccessDecorator(room);
 *       if (chkBreakfast.isSelected()) room = new BreakfastDecorator(room);
 *       if (chkTransfer.isSelected())  room = new AirportTransferDecorator(room);
 *       if (chkLateOut.isSelected())   room = new LateCheckoutDecorator(room);
 *       double effectiveNightly = room.getPricePerNight(); // now includes add-ons
 * ─────────────────────────────────────────────────────────────────────────────
 */
public abstract class RoomDecorator extends Room {

    private static final long serialVersionUID = 1L;

    /** The wrapped Room (could itself be another decorator). */
    protected final Room wrappedRoom;

    protected RoomDecorator(Room room) {
        this.wrappedRoom = room;
    }

    // ── Delegate all base Room getters to the wrapped instance ────────────────
    // Decorators only override what they change (price and amenities).

    @Override public int        getRoomId()         { return wrappedRoom.getRoomId(); }
    @Override public String     getRoomNumber()     { return wrappedRoom.getRoomNumber(); }
    @Override public RoomType   getRoomType()       { return wrappedRoom.getRoomType(); }
    @Override public RoomStatus getStatus()         { return wrappedRoom.getStatus(); }
    @Override public int        getCapacity()       { return wrappedRoom.getCapacity(); }
    @Override public int        getFloor()          { return wrappedRoom.getFloor(); }
    @Override public String     getDescription()    { return wrappedRoom.getDescription(); }
    @Override public String     getRoomCategory()   { return wrappedRoom.getRoomCategory(); }

    /**
     * Default: return the wrapped room's price.
     * Concrete decorators override this to add their surcharge.
     */
    @Override
    public double getPricePerNight() {
        return wrappedRoom.getPricePerNight();
    }

    /**
     * Default: return the wrapped room's amenity string.
     * Concrete decorators append their own label.
     */
    @Override
    public String getAmenities() {
        return wrappedRoom.getAmenities();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Decorator 1 — Spa Access  (+$50 / night)
    // ══════════════════════════════════════════════════════════════════════════

    public static class SpaAccessDecorator extends RoomDecorator {
        private static final long   serialVersionUID = 1L;
        private static final double NIGHTLY_COST     = 50.00;

        public SpaAccessDecorator(Room room) {
            super(room);
        }

        @Override
        public double getPricePerNight() {
            return wrappedRoom.getPricePerNight() + NIGHTLY_COST;
        }

        @Override
        public String getAmenities() {
            return wrappedRoom.getAmenities() + ", Spa Access (+$50/night)";
        }

        @Override public String getRoomCategory() {
            return wrappedRoom.getRoomCategory() + " + Spa";
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Decorator 2 — Breakfast Package  (+$30 / night)
    // ══════════════════════════════════════════════════════════════════════════

    public static class BreakfastDecorator extends RoomDecorator {
        private static final long   serialVersionUID = 1L;
        private static final double NIGHTLY_COST     = 30.00;

        public BreakfastDecorator(Room room) {
            super(room);
        }

        @Override
        public double getPricePerNight() {
            return wrappedRoom.getPricePerNight() + NIGHTLY_COST;
        }

        @Override
        public String getAmenities() {
            return wrappedRoom.getAmenities() + ", Breakfast Package (+$30/night)";
        }

        @Override public String getRoomCategory() {
            return wrappedRoom.getRoomCategory() + " + Breakfast";
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Decorator 3 — Airport Transfer  (+$75 flat fee)
    // The flat fee is spread across the nightly rate for calculation simplicity.
    // Callers can also read getFlatFee() and add it separately.
    // ══════════════════════════════════════════════════════════════════════════

    public static class AirportTransferDecorator extends RoomDecorator {
        private static final long   serialVersionUID = 1L;
        public  static final double FLAT_FEE         = 75.00;

        public AirportTransferDecorator(Room room) {
            super(room);
        }

        /** Flat fee not spread into nightly — callers use getFlatFee() explicitly. */
        @Override
        public double getPricePerNight() {
            return wrappedRoom.getPricePerNight(); // nightly unchanged
        }

        /** The one-time charge added to the reservation total. */
        public double getFlatFee() { return FLAT_FEE; }

        @Override
        public String getAmenities() {
            return wrappedRoom.getAmenities() + ", Airport Transfer (+$75 flat)";
        }

        @Override public String getRoomCategory() {
            return wrappedRoom.getRoomCategory() + " + Transfer";
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Concrete Decorator 4 — Late Checkout  (+$40 flat fee)
    // ══════════════════════════════════════════════════════════════════════════

    public static class LateCheckoutDecorator extends RoomDecorator {
        private static final long   serialVersionUID = 1L;
        public  static final double FLAT_FEE         = 40.00;

        public LateCheckoutDecorator(Room room) {
            super(room);
        }

        @Override
        public double getPricePerNight() {
            return wrappedRoom.getPricePerNight(); // nightly unchanged
        }

        public double getFlatFee() { return FLAT_FEE; }

        @Override
        public String getAmenities() {
            return wrappedRoom.getAmenities() + ", Late Checkout 2 pm (+$40 flat)";
        }

        @Override public String getRoomCategory() {
            return wrappedRoom.getRoomCategory() + " + Late Checkout";
        }
    }


    // ── Utility: unwrap to the original concrete Room ─────────────────────────

    /**
     * Recursively unwraps nested decorators to return the original Room.
     * Useful when you need to persist the base Room (without decorator state).
     */
    public static Room unwrap(Room room) {
        while (room instanceof RoomDecorator) {
            room = ((RoomDecorator) room).wrappedRoom;
        }
        return room;
    }

    /**
     * Total flat fees accumulated in a decorator chain
     * (Airport Transfer + Late Checkout, etc.).
     */
    public static double totalFlatFees(Room room) {
        double total = 0;
        while (room instanceof RoomDecorator) {
            if (room instanceof AirportTransferDecorator)
                total += AirportTransferDecorator.FLAT_FEE;
            if (room instanceof LateCheckoutDecorator)
                total += LateCheckoutDecorator.FLAT_FEE;
            room = ((RoomDecorator) room).wrappedRoom;
        }
        return total;
    }
}
