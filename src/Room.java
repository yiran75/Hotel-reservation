import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private String roomId;
    private RoomType type;
    private BedType bed;
    private int price;

    public Room(String roomId, RoomType type, BedType bed, int price) {
        this.roomId = roomId;
        this.type = type;
        this.bed = bed;
        this.price = price;
    }

    public String getRoomId() { return roomId; }
    public RoomType getType() { return type; }
    public BedType getBed() { return bed; }
    public int getPrice() { return price; }

    @Override
    public String toString() { return roomId; }
}