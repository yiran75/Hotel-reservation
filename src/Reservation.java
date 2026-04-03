import java.io.Serializable;

public class Reservation implements Serializable {
    private final String resId, roomId, custId;
    private final int total;

    private Reservation(Builder b) {
        this.resId = b.resId;
        this.roomId = b.roomId;
        this.custId = b.custId;
        this.total = b.total;
    }

    public static class Builder {
        private String resId, roomId, custId;
        private int total;
        public Builder setResId(String id) { this.resId = id; return this; }
        public Builder setRoomId(String rid) { this.roomId = rid; return this; }
        public Builder setCustId(String c) { this.custId = c; return this; }
        public Builder setTotal(int t) { this.total = t; return this; }
        public Reservation build() { return new Reservation(this); }
    }
}