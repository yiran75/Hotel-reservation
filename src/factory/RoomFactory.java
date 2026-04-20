package factory;

import enums.RoomType;
import model.DeluxeRoom;
import model.Room;
import model.StandardRoom;
import model.SuiteRoom;

public class RoomFactory {

    public abstract static class RoomCreator {

        public abstract Room createRoom(int id, String number, double price,
                                        int capacity, int floor, String description);

        public final Room buildAndLog(int id, String number, double price,
                                      int capacity, int floor, String description) {
            Room room = createRoom(id, number, price, capacity, floor, description);
            System.out.println(room.getRoomCategory()
                               + " | Room #" + room.getRoomNumber()
                               + " | $" + room.getPricePerNight() + "/night");
            return room;
        }
    }

    public static class StandardRoomCreator extends RoomCreator {
        @Override
        public Room createRoom(int id, String number, double price,
                               int capacity, int floor, String description) {
            return new StandardRoom(id, number, price, capacity, floor, description,
                                    true, true, "Double");
        }
    }

    public static class DeluxeRoomCreator extends RoomCreator {
        @Override
        public Room createRoom(int id, String number, double price,
                               int capacity, int floor, String description) {
            return new DeluxeRoom(id, number, price, capacity, floor, description,
                                  true, true, false, "City");
        }
    }

    public static class SuiteRoomCreator extends RoomCreator {
        @Override
        public Room createRoom(int id, String number, double price,
                               int capacity, int floor, String description) {
            return new SuiteRoom(id, number, price, capacity, floor, description,
                                 2, false, true, true);
        }
    }

    public static Room create(RoomType type, int id, String number, double price,
                               int capacity, int floor, String description) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        RoomCreator creator;
        switch (type) {
            case STANDARD:   creator = new StandardRoomCreator(); break;
            case DELUXE:     creator = new DeluxeRoomCreator();   break;
            case SUITE:
            case PENTHOUSE:  creator = new SuiteRoomCreator();    break;
            default:
                throw new IllegalArgumentException();
        }

        return creator.buildAndLog(id, number, price, capacity, floor, description);
    }
}
