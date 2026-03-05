import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String roomId;
    private final RoomType type;
    private final BedType bed;
    private final int price;

    private Room(Builder builder) {
        this.roomId = builder.roomId;
        this.type = builder.type;
        this.bed = builder.bed;
        this.price = builder.price;
    }

    public String getRoomId() { return roomId; }
    public RoomType getType() { return type; }
    public BedType getBed() { return bed; }
    public int getPrice() { return price; }

    @Override
    public String toString() { return roomId; }

    public static class Builder {
        private String roomId;
        private RoomType type;
        private BedType bed;
        private int price;

        public Builder setRoomId(String roomId) { this.roomId = roomId; return this; }
        public Builder setType(RoomType type) { this.type = type; return this; }
        public Builder setBed(BedType bed) { this.bed = bed; return this; }
        public Builder setPrice(int price) { this.price = price; return this; }

        public Room build() {
            return new Room(this);
        }
    }
}
