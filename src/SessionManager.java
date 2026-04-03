public class SessionManager {
    private static SessionManager instance;
    private Customer currentCustomer; 

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(Customer customer) { this.currentCustomer = customer; }
    public Customer getCurrentCustomer() { return currentCustomer; }
    public void logout() { currentCustomer = null; }
}