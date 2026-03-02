import java.io.Serializable;
import java.util.Date;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String resId;
    private final String roomId;
    private final String custId;
    private final Date checkIn;
    private final Date checkOut;
    private final int total;

    private Reservation(Builder builder) {
        this.resId = builder.resId;
        this.roomId = builder.roomId;
        this.custId = builder.custId;
        this.checkIn = builder.checkIn;
        this.checkOut = builder.checkOut;
        this.total = builder.total;
    }

    public String getResId() { return resId; }
    public String getRoomId() { return roomId; }
    public String getCustId() { return custId; }
    public Date getCheckIn() { return checkIn; }
    public Date getCheckOut() { return checkOut; }
    public int getTotal() { return total; }

    

    public static class Builder {
        private String resId;
        private String roomId;
        private String custId;
        private Date checkIn;
        private Date checkOut;
        private int total;

        public Builder setResId(String resId) {
            this.resId = resId;
            return this;
        }

        public Builder setRoomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder setCustId(String custId) {
            this.custId = custId;
            return this;
        }

        public Builder setCheckIn(Date checkIn) {
            this.checkIn = checkIn;
            return this;
        }

        public Builder setCheckOut(Date checkOut) {
            this.checkOut = checkOut;
            return this;
        }

        public Builder setTotal(int total) {
            this.total = total;
            return this;
        }

        public Reservation build() {
            return new Reservation(this);
        }
    }
}