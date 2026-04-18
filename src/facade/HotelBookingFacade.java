package facade;

import builder.ReservationBuilder;
import dao.CustomerDAO;
import dao.ReservationDAO;
import dao.RoomDAO;
import model.Customer;
import model.Reservation;
import model.Room;
import singleton.HotelDatabaseManager;
import strategy.PricingStrategy;

import java.util.Date;
import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PATTERN 4 — FACADE  (Structural)
 * ─────────────────────────────────────────────────────────────────────────────
 * Intent
 *   Provide a unified, simplified interface to a set of interfaces in a
 *   subsystem (DAO layer + Builder + Strategy + Singleton).  The Facade makes
 *   the subsystem easier to use by reducing coupling between the Swing UI and
 *   the business logic.
 *
 * Why it matters here
 *   Before the Facade, ReservationForm.java had to:
 *     1. Fetch the CustomerDAO directly and call findById()
 *     2. Fetch the RoomDAO directly and call findById()
 *     3. Check room availability manually
 *     4. Instantiate a ReservationBuilder and chain all the steps
 *     5. Choose and apply a PricingStrategy
 *     6. Call reservationDAO.save()
 *     7. Optionally update the room status
 *   All of that is now one call:
 *       BookingResult result = facade.bookRoom(customerId, roomId,
 *                                              checkIn, checkOut, guests,
 *                                              strategy, specialRequests);
 *
 * Subsystems hidden behind this Facade
 *   - HotelDatabaseManager (Singleton)      — DAO acquisition
 *   - CustomerDAO / RoomDAO / ReservationDAO — persistence
 *   - ReservationBuilder                    — safe object construction
 *   - PricingStrategy                       — cost calculation
 *
 * How to wire it in (ReservationForm.java)
 *   Add a field:
 *       private final HotelBookingFacade bookingFacade = new HotelBookingFacade();
 *
 *   Replace handleSave() body with:
 *       PricingStrategy strategy = (PricingStrategy) cmbStrategy.getSelectedItem();
 *       HotelBookingFacade.BookingResult result = bookingFacade.bookRoom(
 *           customerId, roomId, checkIn, checkOut, guests, strategy, requests);
 *       if (result.isSuccess()) {
 *           setStatus("Booked! Total: $" + result.getTotalCharged(), true);
 *       } else {
 *           setStatus(result.getMessage(), false);
 *       }
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class HotelBookingFacade {

    // ── Subsystem references (obtained from the Singleton) ────────────────────
    private final CustomerDAO    customerDAO;
    private final RoomDAO        roomDAO;
    private final ReservationDAO reservationDAO;

    public HotelBookingFacade() {
        HotelDatabaseManager db = HotelDatabaseManager.getInstance();
        this.customerDAO    = db.getCustomerDAO();
        this.roomDAO        = db.getRoomDAO();
        this.reservationDAO = db.getReservationDAO();
    }

    // ── Result value object ───────────────────────────────────────────────────

    /**
     * Immutable result object returned to the UI — no raw exceptions leak up.
     */
    public static final class BookingResult {
        private final boolean     success;
        private final String      message;
        private final Reservation reservation;
        private final double      totalCharged;

        private BookingResult(boolean success, String message,
                              Reservation reservation, double totalCharged) {
            this.success     = success;
            this.message     = message;
            this.reservation = reservation;
            this.totalCharged = totalCharged;
        }

        public boolean     isSuccess()      { return success; }
        public String      getMessage()     { return message; }
        public Reservation getReservation() { return reservation; }
        public double      getTotalCharged(){ return totalCharged; }

        static BookingResult ok(Reservation r) {
            return new BookingResult(true,
                "Reservation #" + r.getReservationId() + " confirmed.",
                r, r.getTotalAmount());
        }
        static BookingResult fail(String reason) {
            return new BookingResult(false, reason, null, 0);
        }
    }

    // ── Main Facade method ────────────────────────────────────────────────────

    /**
     * End-to-end booking operation.  The UI calls this single method; all
     * subsystem coordination is hidden inside.
     *
     * Steps performed internally:
     *   1. Validate customer exists
     *   2. Validate room exists
     *   3. Check room is AVAILABLE
     *   4. Build Reservation via ReservationBuilder + PricingStrategy
     *   5. Persist the reservation via ReservationDAO
     *   6. Mark the room as RESERVED via RoomDAO
     *
     * @return BookingResult — always non-null; check isSuccess() before reading fields.
     */
    public BookingResult bookRoom(int customerId, int roomId,
                                  Date checkIn, Date checkOut,
                                  int guests,
                                  PricingStrategy pricingStrategy,
                                  String specialRequests) {

        System.out.println("[Facade] bookRoom() called — customer=" + customerId
            + ", room=" + roomId);

        // Step 1 — customer must exist
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            return BookingResult.fail("Customer ID " + customerId + " not found.");
        }

        // Step 2 — room must exist
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            return BookingResult.fail("Room ID " + roomId + " not found.");
        }

        // Step 3 — room must be available (not occupied, not already reserved)
        if (room.getStatus() != enums.RoomStatus.AVAILABLE) {
            return BookingResult.fail("Room " + room.getRoomNumber()
                + " is currently " + room.getStatus().getDisplayName() + ".");
        }

        // Step 4 — build Reservation via Builder + Strategy
        Reservation reservation;
        try {
            reservation = new ReservationBuilder()
                .forCustomer(customerId)
                .inRoom(roomId)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .withGuests(guests)
                .withSpecialRequests(specialRequests)
                .applyPricing(room.getPricePerNight(), pricingStrategy)
                .build();
        } catch (IllegalStateException e) {
            return BookingResult.fail("Booking validation failed: " + e.getMessage());
        }

        // Step 5 — persist reservation
        boolean saved = reservationDAO.save(reservation);
        if (!saved) {
            return BookingResult.fail("File persistence failed — reservation not saved.");
        }

        // Step 6 — update room status to RESERVED
        room.setStatus(enums.RoomStatus.RESERVED);
        roomDAO.update(room);

        System.out.println("[Facade] Booking complete — reservation #"
            + reservation.getReservationId()
            + ", total=$" + String.format("%.2f", reservation.getTotalAmount()));

        return BookingResult.ok(reservation);
    }

    // ── Additional Facade helpers the UI can call ─────────────────────────────

    /**
     * Cancel a reservation: deletes it and restores the room to AVAILABLE.
     */
    public BookingResult cancelReservation(int reservationId) {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            return BookingResult.fail("Reservation #" + reservationId + " not found.");
        }
        Room room = roomDAO.findById(res.getRoomId());

        boolean deleted = reservationDAO.delete(reservationId);
        if (!deleted) {
            return BookingResult.fail("Could not delete reservation #" + reservationId + ".");
        }

        if (room != null) {
            room.setStatus(enums.RoomStatus.AVAILABLE);
            roomDAO.update(room);
        }

        System.out.println("[Facade] Reservation #" + reservationId + " cancelled.");
        return BookingResult.ok(res);   // returns the now-deleted record for display
    }

    /**
     * Quick availability check — how many rooms are currently AVAILABLE?
     */
    public int countAvailableRooms() {
        return roomDAO.findAvailable().size();
    }

    /**
     * All reservations for a specific guest.
     */
    public List<Reservation> getReservationsForCustomer(int customerId) {
        return reservationDAO.findByCustomerId(customerId);
    }
}
