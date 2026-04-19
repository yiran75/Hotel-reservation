package builder;

import enums.ReservationStatus;
import model.Reservation;
import java.util.Date;

public class ReservationBuilder {

    private int customerId = -1;
    private int roomId = -1;
    private Date checkInDate;
    private Date checkOutDate;
    private int numberOfGuests = 1;
    private double totalAmount = 0.0;
    private double depositAmount = 0.0;
    private ReservationStatus status = ReservationStatus.PENDING;
    private String specialRequests = "";

    public ReservationBuilder forCustomer(int customerId) {
        this.customerId = customerId;
        return this;
    }

    public ReservationBuilder inRoom(int roomId) {
        this.roomId = roomId;
        return this;
    }

    public ReservationBuilder checkIn(Date checkInDate) {
        this.checkInDate = checkInDate;
        return this;
    }

    public ReservationBuilder checkOut(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
        return this;
    }

    public ReservationBuilder withGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
        return this;
    }

    public ReservationBuilder withTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public ReservationBuilder withDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
        return this;
    }

    public ReservationBuilder applyPricing(double basePricePerNight,
                                           strategy.PricingStrategy strategy) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalStateException();
        }
        long nights = Math.max(1,
            (checkOutDate.getTime() - checkInDate.getTime()) / (1000L * 60 * 60 * 24));
        this.totalAmount = strategy.calculateTotal(basePricePerNight, (int) nights);
        this.depositAmount = this.totalAmount * 0.20;
        return this;
    }

    public ReservationBuilder withStatus(ReservationStatus status) {
        this.status = (status != null) ? status : ReservationStatus.PENDING;
        return this;
    }

    public ReservationBuilder withSpecialRequests(String specialRequests) {
        this.specialRequests = (specialRequests != null) ? specialRequests.trim() : "";
        return this;
    }

    public Reservation build() {
        if (customerId < 0) {
            throw new IllegalStateException();
        }
        if (roomId < 0) {
            throw new IllegalStateException();
        }
        if (checkInDate == null) {
            throw new IllegalStateException();
        }
        if (checkOutDate == null) {
            throw new IllegalStateException();
        }
        if (!checkOutDate.after(checkInDate)) {
            throw new IllegalStateException();
        }
        if (numberOfGuests < 1) {
            throw new IllegalStateException();
        }
        if (totalAmount < 0) {
            throw new IllegalStateException();
        }
        if (depositAmount == 0.0 && totalAmount > 0.0) {
            depositAmount = totalAmount * 0.20;
        }

        return new Reservation(customerId, roomId, checkInDate, checkOutDate,
                               numberOfGuests, totalAmount, depositAmount, specialRequests);
    }

    public long nightCount() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return Math.max(1,
            (checkOutDate.getTime() - checkInDate.getTime()) / (1000L * 60 * 60 * 24));
    }
}