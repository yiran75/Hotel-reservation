
import java.io.Serializable;

class User implements Serializable {
    String username, password, role;
    User(String u, String p, String r) { username = u; password = p; role = r; }
}

class Room implements Serializable {
    private final String roomId, type;
    private final int price;

    private Room(Builder b) { this.roomId = b.roomId; this.type = b.type; this.price = b.price; }
    public String getRoomId() { return roomId; }
    public String getType() { return type; }
    public int getPrice() { return price; }

    public static class Builder {
        private String roomId, type; private int price;
        public Builder setRoomId(String id) { this.roomId = id; return this; }
        public Builder setType(String t) { this.type = t; return this; }
        public Builder setPrice(int p) { this.price = p; return this; }
        public Room build() { return new Room(this); }
    }
}
