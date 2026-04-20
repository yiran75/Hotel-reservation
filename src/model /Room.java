package model;

import enums.RoomStatus;
import enums.RoomType;
import java.io.Serializable;

public abstract class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roomId;
    private String roomNumber;
    private RoomType roomType;
    private RoomStatus status;
    private double pricePerNight;
    private int capacity;
    private int floor;
    private String description;

    // Default constructor
    public Room() {
        this.status = RoomStatus.AVAILABLE;
    }

    // Parameterized constructor
    public Room(int roomId, String roomNumber, RoomType roomType,
                double pricePerNight, int capacity, int floor, String description) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = RoomStatus.AVAILABLE;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.floor = floor;
        this.description = description;
    }

    // Abstract method — subclasses define their amenities label
    public abstract String getAmenities();

    // Abstract method — subclasses define display type label
    public abstract String getRoomCategory();

    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Room{id=" + roomId + ", number=" + roomNumber +
               ", type=" + roomType + ", status=" + status +
               ", price=$" + pricePerNight + "/night}";
    }
}
