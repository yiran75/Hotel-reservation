import java.io.Serializable;

public class Room implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum RoomType
    {
        SINGLE, DOUBLE, DELUXE, SUITE
    }

    private String roomId;
    private RoomType type;
    private double pricePerNight;
    private boolean available;

    public Room(String roomId, RoomType type, double pricePerNight)
    {
        this.roomId = roomId;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.available = true;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}