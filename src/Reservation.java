import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Status
    {
        CONFIRMED, CANCELLED, CHECKED_OUT
    }

    private String reservationId;
    private String customerId;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Status status;

    public Reservation(String reservationId, String customerId, String roomId,
                       LocalDate checkInDate, LocalDate checkOutDate)
    {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = Status.CONFIRMED;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}