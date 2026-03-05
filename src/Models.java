
import java.io.Serializable;

class Customer implements Serializable {
    private String id, name, phone, email;

    public Customer(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
}

class Room implements Serializable {
    private final String roomId, type;
    private final int price;

    private Room(Builder b) {
        this.roomId = b.roomId;
        this.type = b.type;
        this.price = b.price;
    }

    public String getRoomId() { return roomId; }
    public int getPrice() { return price; }

    public static class Builder {
        private String roomId, type;
        private int price;
        public Builder setRoomId(String id) { this.roomId = id; return this; }
        public Builder setType(String t) { this.type = t; return this; }
        public Builder setPrice(int p) { this.price = p; return this; }
        public Room build() { return new Room(this); }
    }
}
