import java.io.Serializable;

public class Reservation implements Serializable {
    private final String resId;
    private final String roomId;
    private final String custId;
    private final int total;

    private Reservation(Builder builder) {
        this.resId = builder.resId;
        this.roomId = builder.roomId;
        this.custId = builder.custId;
        this.total = builder.total;
    }

    public static class Builder {
        private String resId;
        private String roomId;
        private String custId;
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

        public Builder setTotal(int total) {
            this.total = total;
            return this;
        }

        public Reservation build() {
            return new Reservation(this);
        }
    }
}
