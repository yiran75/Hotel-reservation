package singleton;

import dao.CustomerDAO;
import dao.ReservationDAO;
import dao.RoomDAO;

public final class HotelDatabaseManager {

    private final CustomerDAO customerDAO;
    private final RoomDAO roomDAO;
    private final ReservationDAO reservationDAO;

    private HotelDatabaseManager() {
        this.customerDAO = new CustomerDAO();
        this.roomDAO = new RoomDAO();
        this.reservationDAO = new ReservationDAO();
    }

    private static final class Holder {
        private static final HotelDatabaseManager INSTANCE = new HotelDatabaseManager();
    }

    public static HotelDatabaseManager getInstance() {
        return Holder.INSTANCE;
    }

    public CustomerDAO getCustomerDAO() {
        return customerDAO;
    }

    public RoomDAO getRoomDAO() {
        return roomDAO;
    }

    public ReservationDAO getReservationDAO() {
        return reservationDAO;
    }

    public void printIdentity() {
        System.out.println(System.identityHashCode(this));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected Object readResolve() {
        return getInstance();
    }
}