import java.io.Serializable;

class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String customerId, name, phone, email;

    public Customer(String id, String name, String phone, String email) {
        this.customerId = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return customerId + " - " + name; }
}

interface ICustomerFactory {
    Customer createCustomer(String id, String name, String phone, String email);
}

class CustomerFactory implements ICustomerFactory {
    @Override
    public Customer createCustomer(String id, String name, String phone, String email) {
        return new Customer(id, name, phone, email);
    }
}

public class Main {
    public static void main(String[] args) {
        ICustomerFactory factory = new CustomerFactory();
        Customer customer = factory.createCustomer("C101", "Olied", "01700000000", "olied@hotel.com");
        System.out.println(customer);
    }
}
